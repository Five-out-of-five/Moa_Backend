package com.fiveoutoffive.moa_backend.mediation.application.service;

import com.fiveoutoffive.moa_backend.ai.application.service.AiGateway;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import com.fiveoutoffive.moa_backend.dashboard.domain.model.WidgetType;
import com.fiveoutoffive.moa_backend.dashboard.domain.repository.RoomWidgetRepository;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.ideacard.application.command.IdeaCardCreateCommand;
import com.fiveoutoffive.moa_backend.ideacard.application.result.IdeaCardDetailResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.service.IdeaCardViewAssembler;
import com.fiveoutoffive.moa_backend.ideacard.application.usecase.IdeaCardUseCase;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.IdeaCard;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.IdeaCardRepository;
import com.fiveoutoffive.moa_backend.ideacard.exception.IdeaCardErrorCode;
import com.fiveoutoffive.moa_backend.mediation.application.command.DraftAdoptCommand;
import com.fiveoutoffive.moa_backend.mediation.application.result.AiCompromisePayload;
import com.fiveoutoffive.moa_backend.mediation.application.result.CompromiseDraftResponse;
import com.fiveoutoffive.moa_backend.mediation.application.result.ConflictDetailResponse;
import com.fiveoutoffive.moa_backend.mediation.application.result.ConflictSummaryResponse;
import com.fiveoutoffive.moa_backend.mediation.application.usecase.MediationUseCase;
import com.fiveoutoffive.moa_backend.mediation.domain.model.CardConflict;
import com.fiveoutoffive.moa_backend.mediation.domain.model.CompromiseDraft;
import com.fiveoutoffive.moa_backend.mediation.domain.model.ConflictStatus;
import com.fiveoutoffive.moa_backend.mediation.domain.model.DraftCardType;
import com.fiveoutoffive.moa_backend.mediation.domain.model.DraftStatus;
import com.fiveoutoffive.moa_backend.mediation.domain.repository.CardConflictRepository;
import com.fiveoutoffive.moa_backend.mediation.domain.repository.CompromiseDraftRepository;
import com.fiveoutoffive.moa_backend.mediation.exception.MediationErrorCode;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Transactional
@RequiredArgsConstructor
public class MediationService implements MediationUseCase {

    private final CardConflictRepository conflictRepository;
    private final CompromiseDraftRepository draftRepository;
    private final RoomWidgetRepository widgetRepository;
    private final IdeaCardRepository cardRepository;
    private final IdeaCardViewAssembler cardAssembler;
    private final IdeaCardUseCase ideaCardUseCase;
    private final RoomAccessService access;
    private final AiGateway aiGateway;
    private final RealtimePublisher realtime;
    private final TransactionTemplate transactionTemplate;

    @Transactional(readOnly = true)
    @Override
    public List<ConflictSummaryResponse> getConflicts(Long accountId, Long roomId, ConflictStatus status) {
        access.requireMember(roomId, accountId);
        requireWidget(roomId);
        return conflictRepository.findByRoomId(roomId).stream().filter(c -> status == null || c.getStatus() == status).map(this::summary).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public ConflictDetailResponse getConflict(Long accountId, Long roomId, Long conflictId) {
        access.requireMember(roomId, accountId);
        requireWidget(roomId);
        CardConflict c = find(roomId, conflictId);
        ConflictDetailResponse.MyDraft draft = draftRepository.findByConflictIdAndRequestedByAccountIdAndStatus(conflictId, accountId, DraftStatus.DRAFT).stream().findFirst()
                .map(d -> new ConflictDetailResponse.MyDraft(d.getId(), d.getCardType(), d.getTitle(), d.getContent(), d.getStatus())).orElse(null);
        return new ConflictDetailResponse(c.getId(), c.getStatus(), c.getReason(), cardAssembler.detail(roomId, accountId, card(roomId, c.getCardAId())), cardAssembler.detail(roomId, accountId, card(roomId, c.getCardBId())), draft, c.getAdoptedCardId());
    }

    @Override
    public ConflictSummaryResponse dismiss(Long accountId, Long roomId, Long conflictId) {
        access.requireMember(roomId, accountId);
        CardConflict c = findForUpdate(roomId, conflictId);
        if (c.getStatus() == ConflictStatus.DISMISSED) {
            throw new BusinessException(MediationErrorCode.CONFLICT_ALREADY_DISMISSED);
        }
        CardConflict saved = conflictRepository.save(c.toBuilder().status(ConflictStatus.DISMISSED).dismissedByAccountId(accountId).dismissedAt(KstTime.nowUtc()).build());
        realtime.toRoom(roomId, "CONFLICT_CHANGED", Map.of("conflictId", conflictId), accountId);
        return summary(saved);
    }

    /**
     * AI 응답(최대 30초)을 기다리는 동안 DB 연결을 붙잡지 않도록 트랜잭션 없이 읽고 호출한 뒤, 저장만 짧게 묶는다.
     * 기다리는 사이 다른 멤버가 채택하거나 해제했을 수 있어 저장 직전에 다시 확인한다. 이때 409 가 나도 AI 호출은
     * 성공했으므로 한도는 돌려주지 않는다.
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CompromiseDraftResponse createDraft(Long accountId, Long roomId, Long conflictId) {
        access.requireMember(roomId, accountId);
        requireWidget(roomId);
        CardConflict c = requireDraftable(find(roomId, conflictId));
        // 내 초안이 이미 열려 있으면 AI 를 다시 부르지 않고 그 초안을 돌려준다(만들기 연타, 재요청).
        Optional<CompromiseDraft> open = myOpenDraft(conflictId, accountId);
        if (open.isPresent()) {
            return toResponse(open.get());
        }
        IdeaCard a = card(roomId, c.getCardAId());
        IdeaCard b = card(roomId, c.getCardBId());
        DraftCardType type = a.getCardType() == CardType.AGENDA || b.getCardType() == CardType.AGENDA ? DraftCardType.AGENDA : DraftCardType.TRY;
        AiCompromisePayload payload = aiGateway.call(roomId, accountId, AiFeature.COMPROMISE, "/v1/compromises",
                Map.of("cardType", type, "cardA", Map.of("title", a.getTitle(), "content", String.valueOf(a.getContent())), "cardB", Map.of("title", b.getTitle(), "content", String.valueOf(b.getContent()))), AiCompromisePayload.class);
        CompromiseDraft draft = transactionTemplate.execute(tx -> {
            CardConflict latest = requireDraftable(findForUpdate(roomId, conflictId));
            // AI 를 기다리는 사이 같은 요청이 먼저 초안을 만들었으면 그것을 돌려준다(초안이 2개 생기지 않게).
            Optional<CompromiseDraft> made = myOpenDraft(conflictId, accountId);
            if (made.isPresent()) {
                return made.get();
            }
            CompromiseDraft saved = draftRepository.save(CompromiseDraft.builder().conflictId(conflictId).requestedByAccountId(accountId).cardType(type).title(payload.title()).content(payload.content()).status(DraftStatus.DRAFT).build());
            conflictRepository.save(latest.toBuilder().status(ConflictStatus.PROPOSED).build());
            return saved;
        });
        return toResponse(draft);
    }

    private CardConflict requireDraftable(CardConflict c) {
        if (c.getAdoptedCardId() != null) {
            throw new BusinessException(MediationErrorCode.CONFLICT_ALREADY_ADOPTED);
        }
        // 해제는 '충돌 아님(오탐)' 결정이고 같은 쌍은 다시 감지하지 않는다. 절충안으로 해제가 풀리지 않게 막는다.
        if (c.getStatus() == ConflictStatus.DISMISSED) {
            throw new BusinessException(MediationErrorCode.CONFLICT_ALREADY_DISMISSED);
        }
        return c;
    }

    @Override
    public IdeaCardDetailResponse adopt(Long accountId, Long roomId, Long conflictId, Long draftId, DraftAdoptCommand command) {
        access.requireMember(roomId, accountId);
        // 충돌 행을 잠그고 시작한다. 채택 버튼 연타나 두 사람의 동시 채택이 차례로 처리되어, 늦은 쪽은 아래 검사에서 409 가 된다(카드 2장 방지).
        CardConflict c = findForUpdate(roomId, conflictId);
        if (c.getAdoptedCardId() != null) {
            throw new BusinessException(MediationErrorCode.CONFLICT_ALREADY_ADOPTED);
        }
        CompromiseDraft draft = myOpenDraft(conflictId, draftId, accountId);
        IdeaCardDetailResponse created = ideaCardUseCase.create(accountId, roomId, new IdeaCardCreateCommand(CardType.valueOf(draft.getCardType().name()), command.title(), command.content(), List.of()));
        Long cardId = created.summary().cardId();
        IdeaCard card = card(roomId, cardId);
        cardRepository.save(card.toBuilder().isAiGenerated(true).build());
        draftRepository.save(draft.toBuilder().status(DraftStatus.ADOPTED).title(command.title()).content(command.content()).adoptedCardId(cardId).build());
        conflictRepository.save(c.toBuilder().adoptedCardId(cardId).build());
        realtime.toRoom(roomId, "CONFLICT_CHANGED", Map.of("conflictId", conflictId), accountId);
        return cardAssembler.detail(roomId, accountId, card(roomId, cardId));
    }

    @Override
    public ConflictSummaryResponse discard(Long accountId, Long roomId, Long conflictId, Long draftId) {
        access.requireMember(roomId, accountId);
        CardConflict c = findForUpdate(roomId, conflictId);
        draftRepository.save(myOpenDraft(conflictId, draftId, accountId).toBuilder().status(DraftStatus.DISCARDED).build());
        // 절충안 진행 중(PROPOSED)이고 채택 전일 때만 다시 감지 상태로 돌린다. 다른 멤버가 해제했거나 이미 채택된 충돌은 그대로 둔다.
        if (c.getStatus() != ConflictStatus.PROPOSED || c.getAdoptedCardId() != null) {
            return summary(c);
        }
        return summary(conflictRepository.save(c.toBuilder().status(ConflictStatus.REDETECTED).build()));
    }

    private CompromiseDraft myOpenDraft(Long conflictId, Long draftId, Long accountId) {
        CompromiseDraft draft = draftRepository.findById(draftId).filter(d -> d.getConflictId().equals(conflictId) && d.getRequestedByAccountId().equals(accountId))
                .orElseThrow(() -> new BusinessException(MediationErrorCode.DRAFT_NOT_FOUND));
        if (draft.getStatus() != DraftStatus.DRAFT) {
            throw new BusinessException(MediationErrorCode.DRAFT_ALREADY_PROCESSED);
        }
        return draft;
    }

    private void requireWidget(Long roomId) {
        if (!widgetRepository.existsByRoomIdAndWidgetType(roomId, WidgetType.MEDIATION)) {
            throw new BusinessException(MediationErrorCode.MEDIATION_WIDGET_REQUIRED);
        }
    }

    private Optional<CompromiseDraft> myOpenDraft(Long conflictId, Long accountId) {
        return draftRepository.findByConflictIdAndRequestedByAccountIdAndStatus(conflictId, accountId, DraftStatus.DRAFT).stream().findFirst();
    }

    private CardConflict findForUpdate(Long roomId, Long conflictId) {
        return conflictRepository.findByIdForUpdate(conflictId).filter(c -> c.getRoomId().equals(roomId)).orElseThrow(() -> new BusinessException(MediationErrorCode.CONFLICT_NOT_FOUND));
    }

    private CardConflict find(Long roomId, Long conflictId) {
        return conflictRepository.findById(conflictId).filter(c -> c.getRoomId().equals(roomId)).orElseThrow(() -> new BusinessException(MediationErrorCode.CONFLICT_NOT_FOUND));
    }

    // 충돌 행이 다른 방 카드를 가리켜도(잘못 저장된 경우) 그 카드를 보여주지 않는다.
    private IdeaCard card(Long roomId, Long cardId) {
        return cardRepository.findById(cardId).filter(c -> c.getRoomId().equals(roomId)).orElseThrow(() -> new BusinessException(IdeaCardErrorCode.CARD_NOT_FOUND));
    }

    private ConflictSummaryResponse summary(CardConflict c) {
        return new ConflictSummaryResponse(c.getId(), c.getStatus(), new ConflictSummaryResponse.CardRef(c.getCardAId(), card(c.getRoomId(), c.getCardAId()).getTitle()), new ConflictSummaryResponse.CardRef(c.getCardBId(), card(c.getRoomId(), c.getCardBId()).getTitle()), c.getDetectedAt());
    }

    private CompromiseDraftResponse toResponse(CompromiseDraft d) {
        return new CompromiseDraftResponse(d.getId(), d.getCardType(), d.getTitle(), d.getContent(), d.getStatus());
    }
}
