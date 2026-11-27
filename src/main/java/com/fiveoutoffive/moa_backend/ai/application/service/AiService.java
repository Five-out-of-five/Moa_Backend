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
import com.fiveoutoffive.moa_backend.global.type.MemberStatus;
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
import com.fiveoutoffive.moa_backend.task.domain.model.Task;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskStatus;
import com.fiveoutoffive.moa_backend.task.domain.repository.TaskRepository;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import com.fiveoutoffive.moa_backend.vote.domain.repository.VoteRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 어시스턴트·회의 요약·톤앤매너. 호출 계약은 docs/ai-server.md */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService implements AiUseCase {

    private static final int MIN_SUMMARY_CARDS = 3;
    private static final int MAX_HISTORY = 100;
    private static final long SSE_TIMEOUT_MILLIS = 120_000;
    private static final String NO_ASSIGNEE = "담당자 없음";
    private static final String LEFT_MEMBER = "나간 멤버";

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
    private final TransactionTemplate transactionTemplate;
    // 어시스턴트 전용 실행기(AssistantStreamExecutorConfig). 같은 타입 빈이 둘이라 필드 이름으로 골라 주입된다.
    // 공용 taskExecutor 를 쓰면 긴 스트림이 충돌 감지·알림 같은 @Async 작업까지 막는다.
    private final java.util.concurrent.Executor assistantStreamExecutor;

    @Override
    public SseEmitter ask(Long accountId, Long roomId, AssistantAskCommand command) {
        access.requireMember(roomId, accountId);
        LocalDate quotaDate = gateway.reserve(roomId, AiFeature.ASSISTANT);
        try {
            if (!aiServerPort.isAvailable()) {
                gateway.log(roomId, accountId, AiFeature.ASSISTANT, System.currentTimeMillis(), AiCallStatus.FAILED, "AI server unavailable");
                throw new BusinessException(AiErrorCode.AI_UNAVAILABLE);
            }
            Map<String, Object> body = Map.of("roomId", roomId, "question", command.question(), "context", roomContext(roomId));
            SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
            assistantStreamExecutor.execute(() -> {
                try {
                    streamAnswer(accountId, roomId, command.question(), body, emitter, quotaDate);
                } catch (Exception e) {
                    // 어떤 실패에도 연결을 열어두지 않는다. 열려 있으면 프론트는 타임아웃까지 응답을 기다린다.
                    log.warn("[AI] 어시스턴트 스트리밍 실패 roomId={}: {}", roomId, e.getMessage());
                    emitter.complete();
                }
            });
            return emitter;
        } catch (RejectedExecutionException e) {
            // 동시 스트림이 꽉 찼다. 큐에 쌓으면 사용자는 열린 스트림에서 아무것도 못 받고 기다리므로 바로 거절한다.
            gateway.release(roomId, AiFeature.ASSISTANT, quotaDate);
            gateway.log(roomId, accountId, AiFeature.ASSISTANT, System.currentTimeMillis(), AiCallStatus.FAILED, "assistant streams busy");
            throw new BusinessException(AiErrorCode.AI_UNAVAILABLE);
        } catch (RuntimeException e) {
            gateway.release(roomId, AiFeature.ASSISTANT, quotaDate);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public CursorResponse<AssistantMessageResponse> getHistory(Long accountId, Long roomId, Long before, int size) {
        access.requireMember(roomId, accountId);
        int limit = Math.clamp(size, 1, MAX_HISTORY);
        MemberRef asker = access.memberRef(roomId, accountId);
        return CursorResponse.of(assistantRepository.findMine(roomId, accountId, before, limit + 1).stream()
                .map(m -> new AssistantMessageResponse(m.getId(), asker, m.getQuestion(), m.getAnswer(), m.getStatus(), referencesOf(m.getReferenceJson()), m.getCreatedAt())).toList(), limit, AssistantMessageResponse::messageId);
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

    // 트랜잭션을 걸지 않는다. AI 응답(최대 30초)을 기다리는 동안 DB 연결을 붙잡지 않도록 저장만 짧게 묶는다.
    @Override
    public SummaryResponse createSummary(Long accountId, Long roomId, SummaryCreateCommand command) {
        access.requireMember(roomId, accountId);
        // ALL 범위는 회차와 무관하다. 요청에 회차 ID 가 와도 쓰지 않는다(다른 방 회차가 요약에 연결되지 않게).
        List<Long> sessionIds = command.scopeType() == SummaryScopeType.ALL || command.sessionIds() == null ? List.of() : command.sessionIds().stream().distinct().toList();
        List<BoardSession> sessions = sessionRepository.findByIdIn(sessionIds).stream().filter(s -> s.getRoomId().equals(roomId) && s.getStatus() == SessionStatus.CLOSED).toList();
        if (command.scopeType() == SummaryScopeType.SESSIONS && (sessions.isEmpty() || sessions.size() != sessionIds.size())) {
            throw new BusinessException(SessionErrorCode.SESSION_NOT_FOUND);
        }
        List<IdeaCard> cards = command.scopeType() == SummaryScopeType.ALL ? cardRepository.findByRoomIdAndDeletedAtIsNull(roomId) : cardRepository.findByRoomIdAndSessionIdInAndDeletedAtIsNull(roomId, sessionIds);
        if (cards.size() < MIN_SUMMARY_CARDS) {
            throw new BusinessException(AiErrorCode.AI_NOT_ENOUGH_CARDS);
        }
        AiSummaryPayload payload = gateway.call(roomId, accountId, AiFeature.SUMMARY, "/v1/summaries", Map.of("maxLength", 1000, "cards", cards.stream().map(this::cardJson).toList()), AiSummaryPayload.class);
        AiSummary saved = transactionTemplate.execute(tx -> {
            AiSummary summary = summaryRepository.save(AiSummary.builder().roomId(roomId).scopeType(command.scopeType()).content(payload.content()).requestedByAccountId(accountId).build());
            sessions.forEach(b -> summarySessionRepository.save(AiSummarySession.builder().summaryId(summary.getId()).sessionId(b.getId()).build()));
            return summary;
        });
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
        // 요약 행 전체를 다시 저장하므로, 카드로 저장하기와 겹쳐 saved_card_id 를 예전 값(null)으로 덮지 않게 같은 잠금으로 차례를 맞춘다.
        AiSummary summary = summaryRepository.findByIdForUpdate(summaryId).filter(s -> s.getRoomId().equals(roomId))
                .orElseThrow(() -> new BusinessException(AiErrorCode.AI_SUMMARY_NOT_FOUND));
        if (!summary.getRequestedByAccountId().equals(accountId)) {
            throw new BusinessException(GlobalErrorCode.ACCESS_DENIED);
        }
        return toResponse(summaryRepository.save(summary.toBuilder().content(command.content()).build()));
    }

    @Transactional
    @Override
    public IdeaCardDetailResponse saveSummaryAsCard(Long accountId, Long roomId, Long summaryId, SummaryToCardCommand command) {
        access.requireMember(roomId, accountId);
        // 요약 행을 잠가 두 번 누르거나 두 사람이 동시에 저장해도 차례로 처리한다.
        AiSummary summary = summaryRepository.findByIdForUpdate(summaryId).filter(s -> s.getRoomId().equals(roomId))
                .orElseThrow(() -> new BusinessException(AiErrorCode.AI_SUMMARY_NOT_FOUND));
        // 이미 카드로 저장한 요약이면 새로 만들지 않고 그 카드를 돌려준다. 저장한 카드가 지워졌으면 다시 만든다.
        Long savedCardId = summary.getSavedCardId();
        if (savedCardId != null && cardRepository.findById(savedCardId).filter(c -> c.getRoomId().equals(roomId) && c.getDeletedAt() == null).isPresent()) {
            return ideaCardUseCase.getDetail(accountId, roomId, savedCardId);
        }
        if (summary.getContent().length() > 1000) {
            throw new BusinessException(GlobalErrorCode.INVALID_REQUEST);
        }
        IdeaCardDetailResponse card = ideaCardUseCase.create(accountId, roomId, new IdeaCardCreateCommand(command.cardType(), command.title(), summary.getContent(), List.of()));
        summaryRepository.save(summary.toBuilder().savedCardId(card.summary().cardId()).build());
        return card;
    }

    // 저장하는 것이 없어 트랜잭션을 걸지 않는다(AI 응답을 기다리는 동안 DB 연결을 붙잡지 않게).
    @Override
    public ToneCheckResponse checkTone(Long accountId, Long roomId, ToneCheckCommand command) {
        access.requireMember(roomId, accountId);
        ToneMode mode = accountRepository.findById(accountId).map(Account::getToneMode).orElse(ToneMode.OFF);
        if (mode == ToneMode.OFF || isDirect(command.channelId())) {
            return new ToneCheckResponse(false, null, mode);
        }
        // 명세: 한도 초과는 429, AI 실패는 원문 그대로 통과. 그래서 선점(429)과 호출(실패 시 반납)을 나눠 부른다.
        LocalDate quotaDate = gateway.reserve(roomId, AiFeature.TONE);
        return gateway.callReserved(roomId, accountId, AiFeature.TONE, quotaDate, "/v1/tone-checks", Map.of("text", command.text(), "toneMode", mode.name()), AiTonePayload.class)
                .map(p -> new ToneCheckResponse(p.needsChange(), p.suggestion(), mode))
                .orElse(new ToneCheckResponse(false, null, mode));
    }

    private boolean isDirect(Long channelId) {
        return channelId != null && channelRepository.findById(channelId).filter(c -> c.getChannelType() == ChannelType.DIRECT).isPresent();
    }

    private void streamAnswer(Long accountId, Long roomId, String question, Map<String, Object> body, SseEmitter emitter, LocalDate quotaDate) {
        StringBuilder answer = new StringBuilder();
        StringBuilder delivered = new StringBuilder(); // 사용자에게 실제로 보낸 데까지
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
            if (send(emitter, "delta", Map.of("text", text), clientGone)) {
                delivered.append(text);
            }
        });
        // 명세(API 131, 요구사항 66): 사용자가 스트림을 끊으면 받은 데까지만 PARTIAL 로 저장한다(받은 게 없으면 FAILED).
        // 끊긴 뒤에 AI 서버가 보낸 나머지는 저장하지 않는다.
        String saveText = clientGone.get() ? delivered.toString() : answer.toString();
        AssistantMessageStatus status = completed && !clientGone.get() ? AssistantMessageStatus.COMPLETED : saveText.isEmpty() ? AssistantMessageStatus.FAILED : AssistantMessageStatus.PARTIAL;
        // 한도는 질문 시점에 선점했다. 답이 하나도 없으면(FAILED) 돌려준다. COMPLETED·PARTIAL 만 차감(docs/ai-server.md).
        if (status == AssistantMessageStatus.FAILED) {
            gateway.release(roomId, AiFeature.ASSISTANT, quotaDate);
        }
        AiAssistantMessage saved = assistantRepository.save(AiAssistantMessage.builder().roomId(roomId).accountId(accountId).question(question).answer(saveText).status(status).referenceJson(references[0]).build());
        gateway.log(roomId, accountId, AiFeature.ASSISTANT, startedAt, status == AssistantMessageStatus.FAILED ? AiCallStatus.FAILED : AiCallStatus.SUCCESS, null);
        if (status != AssistantMessageStatus.FAILED) {
            send(emitter, "done", Map.of("messageId", saved.getId(), "status", status), clientGone);
        } else {
            send(emitter, "error", Map.of("code", AiErrorCode.AI_UNAVAILABLE.getCode()), clientGone);
        }
        emitter.complete();
    }

    /** 보냈으면 true. 보내다 실패하면 사용자가 스트림을 끊은 것으로 보고 이후 이벤트는 보내지 않는다. */
    private boolean send(SseEmitter emitter, String event, Object data, AtomicBoolean clientGone) {
        if (clientGone.get()) {
            return false;
        }
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
            return true;
        } catch (Exception e) {
            clientGone.set(true);
            return false;
        }
    }

    /**
     * 이 방의 데이터만 근거로 넘긴다. "누가 썼어?", "마감 임박한 업무는?" 에 답할 수 있게 작성자·담당자·마감일과 오늘 날짜를 함께 보낸다.
     * 사람 이름은 방 표시 이름(MemberRef.displayName)만 보낸다. 나간·탈퇴 멤버는 이름 대신 "나간 멤버" 로 보낸다.
     */
    private Map<String, Object> roomContext(Long roomId) {
        // Python 은 앞에서부터 카드·업무 150개씩만 쓴다(개수는 전체로 센다). 큰 방에서 최근 것이 빠지지 않게
        // 카드는 최신순, 업무는 끝나지 않은 것을 먼저 두고 최신순으로 보낸다. 조회 쿼리에는 정렬이 없다.
        List<IdeaCard> cards = cardRepository.findByRoomIdAndDeletedAtIsNull(roomId).stream()
                .sorted(Comparator.comparing(IdeaCard::getId).reversed()).toList();
        List<Task> tasks = taskRepository.findByRoomIdAndDeletedAtIsNull(roomId).stream()
                .sorted(Comparator.comparing((Task t) -> t.getStatus() == TaskStatus.DONE).thenComparing(Task::getId, Comparator.reverseOrder())).toList();
        Set<Long> people = new HashSet<>();
        cards.forEach(c -> people.add(c.getWriterAccountId()));
        tasks.forEach(t -> people.add(t.getAssigneeAccountId()));
        Map<Long, MemberRef> refs = access.memberRefs(roomId, people);
        return Map.of(
                "today", KstTime.todayKst().toString(),
                "cards", cards.stream().map(c -> {
                    Map<String, Object> json = new LinkedHashMap<>(cardJson(c));
                    json.put("writer", nameOf(refs, c.getWriterAccountId()));
                    return json;
                }).toList(),
                "tasks", tasks.stream().map(t -> taskJson(t, refs)).toList(),
                "votes", voteRepository.findByRoomIdAndStatusAndDeletedAtIsNull(roomId, VoteStatus.OPEN).stream().map(v -> Map.of("id", v.getId(), "title", v.getTitle())).toList(),
                "moods", moodRepository.findByRoomIdAndRecordedDate(roomId, KstTime.todayKst()).stream().map(m -> Map.of("mood", m.getMood())).toList());
    }

    private Map<String, Object> taskJson(Task t, Map<Long, MemberRef> refs) {
        // 마감일이 없을 수 있어 null 을 담을 수 있는 맵을 쓴다(Map.of 는 null 불가).
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", t.getId());
        json.put("title", t.getTitle());
        json.put("status", t.getStatus());
        json.put("assignee", t.getAssigneeAccountId() == null ? NO_ASSIGNEE : nameOf(refs, t.getAssigneeAccountId()));
        json.put("dueDate", t.getDueDate() == null ? null : t.getDueDate().toString());
        return json;
    }

    private String nameOf(Map<Long, MemberRef> refs, Long accountId) {
        MemberRef ref = refs.get(accountId);
        return ref == null || ref.memberStatus() != MemberStatus.ACTIVE || ref.displayName() == null ? LEFT_MEMBER : ref.displayName();
    }

    private Map<String, Object> cardJson(IdeaCard c) {
        return Map.of("id", c.getId(), "type", c.getCardType(), "title", c.getTitle(), "content", c.getContent() == null ? "" : c.getContent());
    }

    /** 저장된 근거 JSON 을 객체로 돌려준다. 질문 스트림의 references 이벤트와 같은 모양이다. 읽을 수 없으면 근거 없음으로 본다. */
    private tools.jackson.databind.JsonNode referencesOf(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return jsonMapper.readTree(json);
        } catch (RuntimeException e) {
            log.warn("[AI] 어시스턴트 근거 JSON 을 읽지 못함: {}", e.getMessage());
            return null;
        }
    }

    private SummaryResponse toResponse(AiSummary s) {
        // 이미 저장된 잘못된 연결(다른 방 회차)이 있어도 응답에는 이 방 회차만 보인다.
        List<SummaryResponse.SessionRef> sessions = sessionRepository.findByIdIn(summarySessionRepository.findBySummaryId(s.getId()).stream().map(AiSummarySession::getSessionId).toList()).stream()
                .filter(b -> b.getRoomId().equals(s.getRoomId()))
                .map(b -> new SummaryResponse.SessionRef(b.getId(), b.getSessionNo(), b.getTitle())).toList();
        return new SummaryResponse(s.getId(), s.getScopeType(), sessions, s.getContent(), access.memberRef(s.getRoomId(), s.getRequestedByAccountId()), s.getSavedCardId(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
