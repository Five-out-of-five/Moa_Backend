package com.fiveoutoffive.moa_backend.ai.application.service;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.model.ToneMode;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.ai.application.command.AssistantAskCommand;
import com.fiveoutoffive.moa_backend.ai.application.command.SummaryCreateCommand;
import com.fiveoutoffive.moa_backend.ai.application.command.SummaryToCardCommand;
import com.fiveoutoffive.moa_backend.ai.application.command.SummaryUpdateCommand;
import com.fiveoutoffive.moa_backend.ai.application.command.ToneCheckCommand;
import com.fiveoutoffive.moa_backend.ai.application.result.AiQuotaResponse;
import com.fiveoutoffive.moa_backend.ai.application.result.AiSummaryPayload;
import com.fiveoutoffive.moa_backend.ai.application.result.AiTonePayload;
import com.fiveoutoffive.moa_backend.ai.application.result.AssistantMessageResponse;
import com.fiveoutoffive.moa_backend.ai.application.result.SummaryResponse;
import com.fiveoutoffive.moa_backend.ai.application.result.ToneCheckResponse;
import com.fiveoutoffive.moa_backend.ai.application.usecase.AiUseCase;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiAssistantMessage;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiCallStatus;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiSummary;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiSummarySession;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiUsageQuota;
import com.fiveoutoffive.moa_backend.ai.domain.model.AssistantMessageStatus;
import com.fiveoutoffive.moa_backend.ai.domain.model.SummaryScopeType;
import com.fiveoutoffive.moa_backend.ai.domain.repository.AiAssistantMessageRepository;
import com.fiveoutoffive.moa_backend.ai.domain.repository.AiSummaryRepository;
import com.fiveoutoffive.moa_backend.ai.domain.repository.AiSummarySessionRepository;
import com.fiveoutoffive.moa_backend.ai.domain.repository.AiUsageQuotaRepository;
import com.fiveoutoffive.moa_backend.ai.exception.AiErrorCode;
import com.fiveoutoffive.moa_backend.chat.domain.model.ChannelType;
import com.fiveoutoffive.moa_backend.chat.domain.repository.ChatChannelRepository;
import com.fiveoutoffive.moa_backend.global.common.api.response.CursorResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.exception.GlobalErrorCode;
import com.fiveoutoffive.moa_backend.global.port.out.AiServerPort;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.ideacard.application.command.IdeaCardCreateCommand;
import com.fiveoutoffive.moa_backend.ideacard.application.result.IdeaCardDetailResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.usecase.IdeaCardUseCase;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.IdeaCard;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.IdeaCardRepository;
import com.fiveoutoffive.moa_backend.mood.domain.repository.MoodStatusRepository;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import com.fiveoutoffive.moa_backend.session.domain.model.BoardSession;
import com.fiveoutoffive.moa_backend.session.domain.model.SessionStatus;
import com.fiveoutoffive.moa_backend.session.domain.repository.BoardSessionRepository;
import com.fiveoutoffive.moa_backend.session.exception.SessionErrorCode;
import com.fiveoutoffive.moa_backend.task.domain.repository.TaskRepository;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import com.fiveoutoffive.moa_backend.vote.domain.repository.VoteRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 어시스턴트·회의 요약·톤앤매너. 호출 계약은 docs/ai-server.md */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService implements AiUseCase {

    private static final int MIN_SUMMARY_CARDS = 3;
    private static final int MAX_HISTORY = 100;
    private static final long SSE_TIMEOUT_MILLIS = 120_000;

    private final AiGateway gateway;
    private final AiServerPort aiServerPort;
    private final AiAssistantMessageRepository assistantRepository;
    private final AiSummaryRepository summaryRepository;
    private final AiSummarySessionRepository summarySessionRepository;
    private final AiUsageQuotaRepository quotaRepository;
    private final AiQuotaProperties quotaProperties;
    private final IdeaCardRepository cardRepository;
    private final TaskRepository taskRepository;
    private final VoteRepository voteRepository;
    private final MoodStatusRepository moodRepository;
    private final BoardSessionRepository sessionRepository;
    private final AccountRepository accountRepository;
    private final ChatChannelRepository channelRepository;
    private final IdeaCardUseCase ideaCardUseCase;
    private final RoomAccessService access;
    private final tools.jackson.databind.json.JsonMapper jsonMapper;
    private final java.util.concurrent.Executor taskExecutor;

    @Override
    public SseEmitter ask(Long accountId, Long roomId, AssistantAskCommand command) {
        access.requireMember(roomId, accountId);
        gateway.requireQuota(roomId, AiFeature.ASSISTANT);
        if (!aiServerPort.isAvailable()) {
            throw new BusinessException(AiErrorCode.AI_UNAVAILABLE);
        }
        Map<String, Object> body = Map.of("roomId", roomId, "question", command.question(), "context", roomContext(roomId));
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        taskExecutor.execute(() -> {
            try {
                streamAnswer(accountId, roomId, command.question(), body, emitter);
            } catch (Exception e) {
                // 어떤 실패에도 연결을 열어두지 않는다. 열려 있으면 프론트는 타임아웃까지 응답을 기다린다.
                log.warn("[AI] 어시스턴트 스트리밍 실패 roomId={}: {}", roomId, e.getMessage());
                emitter.complete();
            }
        });
        return emitter;
    }

    @Transactional(readOnly = true)
    @Override
    public CursorResponse<AssistantMessageResponse> getHistory(Long accountId, Long roomId, Long before, int size) {
        access.requireMember(roomId, accountId);
        int limit = Math.clamp(size, 1, MAX_HISTORY);
        MemberRef asker = access.memberRef(roomId, accountId);
        return CursorResponse.of(assistantRepository.findMine(roomId, accountId, before, limit + 1).stream()
                .map(m -> new AssistantMessageResponse(m.getId(), asker, m.getQuestion(), m.getAnswer(), m.getStatus(), m.getReferenceJson(), m.getCreatedAt())).toList(), limit, AssistantMessageResponse::messageId);
    }

    @Transactional(readOnly = true)
    @Override
    public AiQuotaResponse getQuota(Long accountId, Long roomId) {
        access.requireMember(roomId, accountId);
        LocalDate today = KstTime.todayKst();
        Map<AiFeature, Integer> used = quotaRepository.findByRoomIdAndQuotaDate(roomId, today).stream().collect(Collectors.toMap(AiUsageQuota::getFeature, AiUsageQuota::getUsedCount));
        LocalDateTime resetAt = KstTime.startOfDayUtc(today.plusDays(1));
        return new AiQuotaResponse(List.of(AiFeature.ASSISTANT, AiFeature.SUMMARY, AiFeature.COMPROMISE, AiFeature.TONE, AiFeature.LAYOUT).stream()
                .map(f -> new AiQuotaResponse.Feature(f, used.getOrDefault(f, 0), quotaProperties.limitOf(f), resetAt)).toList());
    }

    @Transactional
    @Override
    public SummaryResponse createSummary(Long accountId, Long roomId, SummaryCreateCommand command) {
        access.requireMember(roomId, accountId);
        List<Long> sessionIds = command.sessionIds() == null ? List.of() : command.sessionIds();
        List<BoardSession> sessions = sessionRepository.findByIdIn(sessionIds).stream().filter(s -> s.getRoomId().equals(roomId) && s.getStatus() == SessionStatus.CLOSED).toList();
        if (command.scopeType() == SummaryScopeType.SESSIONS && (sessions.isEmpty() || sessions.size() != sessionIds.size())) {
            throw new BusinessException(SessionErrorCode.SESSION_NOT_FOUND);
        }
        List<IdeaCard> cards = command.scopeType() == SummaryScopeType.ALL ? cardRepository.findByRoomIdAndDeletedAtIsNull(roomId) : cardRepository.findByRoomIdAndSessionIdInAndDeletedAtIsNull(roomId, sessionIds);
        if (cards.size() < MIN_SUMMARY_CARDS) {
            throw new BusinessException(AiErrorCode.AI_NOT_ENOUGH_CARDS);
        }
        AiSummaryPayload payload = gateway.call(roomId, accountId, AiFeature.SUMMARY, "/v1/summaries", Map.of("maxLength", 1000, "cards", cards.stream().map(this::cardJson).toList()), AiSummaryPayload.class);
        AiSummary saved = summaryRepository.save(AiSummary.builder().roomId(roomId).scopeType(command.scopeType()).content(payload.content()).requestedByAccountId(accountId).build());
        sessionIds.forEach(id -> summarySessionRepository.save(AiSummarySession.builder().summaryId(saved.getId()).sessionId(id).build()));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<SummaryResponse> getSummaries(Long accountId, Long roomId, Pageable pageable) {
        access.requireMember(roomId, accountId);
        return Pages.response(summaryRepository.findByRoomId(roomId, pageable).map(this::toResponse));
    }

    @Transactional
    @Override
    public SummaryResponse updateSummary(Long accountId, Long roomId, Long summaryId, SummaryUpdateCommand command) {
        access.requireMember(roomId, accountId);
        AiSummary summary = findSummary(roomId, summaryId);
        if (!summary.getRequestedByAccountId().equals(accountId)) {
            throw new BusinessException(GlobalErrorCode.ACCESS_DENIED);
        }
        return toResponse(summaryRepository.save(summary.toBuilder().content(command.content()).build()));
    }

    @Transactional
    @Override
    public IdeaCardDetailResponse saveSummaryAsCard(Long accountId, Long roomId, Long summaryId, SummaryToCardCommand command) {
        access.requireMember(roomId, accountId);
        AiSummary summary = findSummary(roomId, summaryId);
        if (summary.getContent().length() > 1000) {
            throw new BusinessException(GlobalErrorCode.INVALID_REQUEST);
        }
        IdeaCardDetailResponse card = ideaCardUseCase.create(accountId, roomId, new IdeaCardCreateCommand(command.cardType(), command.title(), summary.getContent(), List.of()));
        summaryRepository.save(summary.toBuilder().savedCardId(card.summary().cardId()).build());
        return card;
    }

    @Transactional
    @Override
    public ToneCheckResponse checkTone(Long accountId, Long roomId, ToneCheckCommand command) {
        access.requireMember(roomId, accountId);
        ToneMode mode = accountRepository.findById(accountId).map(Account::getToneMode).orElse(ToneMode.OFF);
        if (mode == ToneMode.OFF || isDirect(command.channelId())) {
            return new ToneCheckResponse(false, null, mode);
        }
        if (gateway.remaining(roomId, AiFeature.TONE) <= 0) {
            throw new BusinessException(AiErrorCode.AI_QUOTA_EXCEEDED);
        }
        return gateway.callQuietly(roomId, accountId, AiFeature.TONE, "/v1/tone-checks", Map.of("text", command.text(), "toneMode", mode.name()), AiTonePayload.class)
                .map(p -> new ToneCheckResponse(p.needsChange(), p.suggestion(), mode))
                .orElse(new ToneCheckResponse(false, null, mode));
    }

    private boolean isDirect(Long channelId) {
        return channelId != null && channelRepository.findById(channelId).filter(c -> c.getChannelType() == ChannelType.DIRECT).isPresent();
    }

    private void streamAnswer(Long accountId, Long roomId, String question, Map<String, Object> body, SseEmitter emitter) {
        StringBuilder answer = new StringBuilder();
        AtomicBoolean clientGone = new AtomicBoolean(false);
        String[] references = {null};
        long startedAt = System.currentTimeMillis();
        boolean completed = aiServerPort.stream("/v1/assistant/stream", body, line -> {
            var node = jsonMapper.readTree(line);
            if (node.has("references")) {
                references[0] = node.get("references").toString();
                send(emitter, "references", node.get("references"), clientGone);
                return;
            }
            String text = node.path("delta").asString("");
            answer.append(text);
            send(emitter, "delta", Map.of("text", text), clientGone);
        });
        AssistantMessageStatus status = completed && !clientGone.get() ? AssistantMessageStatus.COMPLETED : answer.isEmpty() ? AssistantMessageStatus.FAILED : AssistantMessageStatus.PARTIAL;
        AiAssistantMessage saved = assistantRepository.save(AiAssistantMessage.builder().roomId(roomId).accountId(accountId).question(question).answer(answer.toString()).status(status).referenceJson(references[0]).build());
        gateway.log(roomId, accountId, AiFeature.ASSISTANT, startedAt, status == AssistantMessageStatus.FAILED ? AiCallStatus.FAILED : AiCallStatus.SUCCESS, null);
        if (status != AssistantMessageStatus.FAILED) {
            gateway.consume(roomId, AiFeature.ASSISTANT);
            send(emitter, "done", Map.of("messageId", saved.getId(), "status", status), clientGone);
        } else {
            send(emitter, "error", Map.of("code", AiErrorCode.AI_UNAVAILABLE.getCode()), clientGone);
        }
        emitter.complete();
    }

    private void send(SseEmitter emitter, String event, Object data, AtomicBoolean clientGone) {
        if (clientGone.get()) {
            return;
        }
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (Exception e) {
            clientGone.set(true); // 사용자가 스트림을 끊었다. 받은 데까지만 저장한다.
        }
    }

    /** 이 방의 데이터만 근거로 넘긴다. */
    private Map<String, Object> roomContext(Long roomId) {
        return Map.of(
                "cards", cardRepository.findByRoomIdAndDeletedAtIsNull(roomId).stream().map(this::cardJson).toList(),
                "tasks", taskRepository.findByRoomIdAndDeletedAtIsNull(roomId).stream().map(t -> Map.of("id", t.getId(), "title", t.getTitle(), "status", t.getStatus())).toList(),
                "votes", voteRepository.findByRoomIdAndStatusAndDeletedAtIsNull(roomId, VoteStatus.OPEN).stream().map(v -> Map.of("id", v.getId(), "title", v.getTitle())).toList(),
                "moods", moodRepository.findByRoomIdAndRecordedDate(roomId, KstTime.todayKst()).stream().map(m -> Map.of("mood", m.getMood())).toList());
    }

    private Map<String, Object> cardJson(IdeaCard c) {
        return Map.of("id", c.getId(), "type", c.getCardType(), "title", c.getTitle(), "content", c.getContent() == null ? "" : c.getContent());
    }

    private AiSummary findSummary(Long roomId, Long summaryId) {
        return summaryRepository.findById(summaryId).filter(s -> s.getRoomId().equals(roomId)).orElseThrow(() -> new BusinessException(AiErrorCode.AI_SUMMARY_NOT_FOUND));
    }

    private SummaryResponse toResponse(AiSummary s) {
        List<SummaryResponse.SessionRef> sessions = sessionRepository.findByIdIn(summarySessionRepository.findBySummaryId(s.getId()).stream().map(AiSummarySession::getSessionId).toList()).stream()
                .map(b -> new SummaryResponse.SessionRef(b.getId(), b.getSessionNo(), b.getTitle())).toList();
        return new SummaryResponse(s.getId(), s.getScopeType(), sessions, s.getContent(), access.memberRef(s.getRoomId(), s.getRequestedByAccountId()), s.getSavedCardId(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
