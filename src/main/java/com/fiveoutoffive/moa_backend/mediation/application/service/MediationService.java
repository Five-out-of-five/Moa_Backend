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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return new ConflictDetailResponse(c.getId(), c.getStatus(), c.getReason(), cardAssembler.detail(roomId, accountId, card(c.getCardAId())), cardAssembler.detail(roomId, accountId, card(c.getCardBId())), draft, c.getAdoptedCardId());
    }

    @Override
    public ConflictSummaryResponse dismiss(Long accountId, Long roomId, Long conflictId) {
        access.requireMember(roomId, accountId);
        CardConflict c = find(roomId, conflictId);
        if (c.getStatus() == ConflictStatus.DISMISSED) {
            throw new BusinessException(MediationErrorCode.CONFLICT_ALREADY_DISMISSED);
        }
        CardConflict saved = conflictRepository.save(c.toBuilder().status(ConflictStatus.DISMISSED).dismissedByAccountId(accountId).dismissedAt(KstTime.nowUtc()).build());
        realtime.toRoom(roomId, "CONFLICT_CHANGED", Map.of("conflictId", conflictId), accountId);
        return summary(saved);
    }

    @Override
    public CompromiseDraftResponse createDraft(Long accountId, Long roomId, Long conflictId) {
        access.requireMember(roomId, accountId);
        requireWidget(roomId);
        CardConflict c = find(roomId, conflictId);
        if (c.getAdoptedCardId() != null) {
            throw new BusinessException(MediationErrorCode.CONFLICT_ALREADY_ADOPTED);
        }
        // 해제는 '충돌 아님(오탐)' 결정이고 같은 쌍은 다시 감지하지 않는다. 절충안으로 해제가 풀리지 않게 막는다.
        if (c.getStatus() == ConflictStatus.DISMISSED) {
            throw new BusinessException(MediationErrorCode.CONFLICT_ALREADY_DISMISSED);
        }
        IdeaCard a = card(c.getCardAId());
        IdeaCard b = card(c.getCardBId());
        DraftCardType type = a.getCardType() == CardType.AGENDA || b.getCardType() == CardType.AGENDA ? DraftCardType.AGENDA : DraftCardType.TRY;
        AiCompromisePayload payload = aiGateway.call(roomId, accountId, AiFeature.COMPROMISE, "/v1/compromises",
                Map.of("cardType", type, "cardA", Map.of("title", a.getTitle(), "content", String.valueOf(a.getContent())), "cardB", Map.of("title", b.getTitle(), "content", String.valueOf(b.getContent()))), AiCompromisePayload.class);
        CompromiseDraft draft = draftRepository.save(CompromiseDraft.builder().conflictId(conflictId).requestedByAccountId(accountId).cardType(type).title(payload.title()).content(payload.content()).status(DraftStatus.DRAFT).build());
        conflictRepository.save(c.toBuilder().status(ConflictStatus.PROPOSED).build());
        return toResponse(draft);
    }

    @Override
    public IdeaCardDetailResponse adopt(Long accountId, Long roomId, Long conflictId, Long draftId, DraftAdoptCommand command) {
        access.requireMember(roomId, accountId);
        CardConflict c = find(roomId, conflictId);
        if (c.getAdoptedCardId() != null) {
            throw new BusinessException(MediationErrorCode.CONFLICT_ALREADY_ADOPTED);
        }
        CompromiseDraft draft = myOpenDraft(conflictId, draftId, accountId);
        IdeaCardDetailResponse created = ideaCardUseCase.create(accountId, roomId, new IdeaCardCreateCommand(CardType.valueOf(draft.getCardType().name()), command.title(), command.content(), List.of()));
        Long cardId = created.summary().cardId();
        IdeaCard card = card(cardId);
        cardRepository.save(card.toBuilder().isAiGenerated(true).build());
        draftRepository.save(draft.toBuilder().status(DraftStatus.ADOPTED).title(command.title()).content(command.content()).adoptedCardId(cardId).build());
        conflictRepository.save(c.toBuilder().adoptedCardId(cardId).build());
        realtime.toRoom(roomId, "CONFLICT_CHANGED", Map.of("conflictId", conflictId), accountId);
        return cardAssembler.detail(roomId, accountId, card(cardId));
    }

    @Override
    public ConflictSummaryResponse discard(Long accountId, Long roomId, Long conflictId, Long draftId) {
        access.requireMember(roomId, accountId);
        CardConflict c = find(roomId, conflictId);
        draftRepository.save(myOpenDraft(conflictId, draftId, accountId).toBuilder().status(DraftStatus.DISCARDED).build());
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

    private CardConflict find(Long roomId, Long conflictId) {
        return conflictRepository.findById(conflictId).filter(c -> c.getRoomId().equals(roomId)).orElseThrow(() -> new BusinessException(MediationErrorCode.CONFLICT_NOT_FOUND));
    }

    private IdeaCard card(Long cardId) {
        return cardRepository.findById(cardId).orElseThrow(() -> new BusinessException(IdeaCardErrorCode.CARD_NOT_FOUND));
    }

    private ConflictSummaryResponse summary(CardConflict c) {
        return new ConflictSummaryResponse(c.getId(), c.getStatus(), new ConflictSummaryResponse.CardRef(c.getCardAId(), card(c.getCardAId()).getTitle()), new ConflictSummaryResponse.CardRef(c.getCardBId(), card(c.getCardBId()).getTitle()), c.getDetectedAt());
    }

    private CompromiseDraftResponse toResponse(CompromiseDraft d) {
        return new CompromiseDraftResponse(d.getId(), d.getCardType(), d.getTitle(), d.getContent(), d.getStatus());
    }
}
