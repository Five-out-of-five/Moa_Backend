package com.fiveoutoffive.moa_backend.mediation.application.service;

import com.fiveoutoffive.moa_backend.ai.application.result.AiEmbeddingPayload;
import com.fiveoutoffive.moa_backend.ai.application.service.AiGateway;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import com.fiveoutoffive.moa_backend.ai.domain.repository.ContentEmbeddingRepository;
import com.fiveoutoffive.moa_backend.dashboard.domain.model.WidgetType;
import com.fiveoutoffive.moa_backend.dashboard.domain.repository.RoomWidgetRepository;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.ideacard.domain.event.CardChangedEvent;
import com.fiveoutoffive.moa_backend.ideacard.domain.event.CardDeletedEvent;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.IdeaCard;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.IdeaCardRepository;
import com.fiveoutoffive.moa_backend.mediation.application.result.AiConflictPayload;
import com.fiveoutoffive.moa_backend.mediation.domain.model.CardConflict;
import com.fiveoutoffive.moa_backend.mediation.domain.model.ConflictStatus;
import com.fiveoutoffive.moa_backend.mediation.domain.repository.CardConflictRepository;
import com.fiveoutoffive.moa_backend.session.domain.event.SessionClosedEvent;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 카드 임베딩 갱신과 AI 충돌 감지. 커밋 후 비동기로 돌며, AI 서버가 없어도 사용자 요청에는 영향이 없다.
 * 해제된 쌍은 다시 감지하지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConflictDetectionHandlers {

    private static final int NEAREST_LIMIT = 10;

    private final CardConflictRepository conflictRepository;
    private final ContentEmbeddingRepository embeddingRepository;
    private final RoomWidgetRepository widgetRepository;
    private final IdeaCardRepository cardRepository;
    private final AiGateway aiGateway;
    private final RealtimePublisher realtime;

    @EventListener
    public void onCardDeleted(CardDeletedEvent event) {
        conflictRepository.deleteByCardId(event.cardId());
        embeddingRepository.deleteByCardId(event.cardId());
    }

    @Async
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @org.springframework.transaction.event.TransactionalEventListener
    public void onCardChanged(CardChangedEvent event) {
        try {
            refreshEmbedding(event.roomId(), event.cardId());
            if (event.created() && widgetRepository.existsByRoomIdAndWidgetType(event.roomId(), WidgetType.MEDIATION)) {
                cardRepository.findById(event.cardId()).ifPresent(this::detect);
            }
        } catch (Exception e) {
            log.warn("[AI] 카드 후처리 실패 cardId={}: {}", event.cardId(), e.getMessage());
        }
    }

    @Async
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @org.springframework.transaction.event.TransactionalEventListener
    public void onSessionClosed(SessionClosedEvent event) {
        try {
            if (widgetRepository.existsByRoomIdAndWidgetType(event.roomId(), WidgetType.MEDIATION)) {
                cardRepository.findBySessionIdAndDeletedAtIsNullOrderBySortOrderAsc(event.sessionId()).forEach(this::detect);
            }
        } catch (Exception e) {
            log.warn("[AI] 회차 충돌 분석 실패 sessionId={}: {}", event.sessionId(), e.getMessage());
        }
    }

    private void refreshEmbedding(Long roomId, Long cardId) throws Exception {
        IdeaCard card = cardRepository.findById(cardId).orElse(null);
        if (card == null) {
            return;
        }
        String text = card.getTitle() + "\n" + (card.getContent() == null ? "" : card.getContent());
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
        if (hash.equals(embeddingRepository.findHashByCardId(cardId).orElse(null))) {
            return;
        }
        aiGateway.callQuietly(roomId, card.getWriterAccountId(), AiFeature.CONFLICT, "/v1/embeddings", Map.of("text", text), AiEmbeddingPayload.class)
                .ifPresent(p -> embeddingRepository.upsertCard(roomId, cardId, p.embedding().toString(), p.model(), hash));
    }

    private void detect(IdeaCard card) {
        List<IdeaCard> candidates = cardRepository.findByIdIn(embeddingRepository.findNearestCardIds(card.getRoomId(), card.getId(), NEAREST_LIMIT));
        if (candidates.isEmpty()) {
            return;
        }
        aiGateway.callQuietly(card.getRoomId(), null, AiFeature.CONFLICT, "/v1/conflicts/detect",
                Map.of("card", Map.of("id", card.getId(), "title", card.getTitle(), "content", String.valueOf(card.getContent())),
                        "candidates", candidates.stream().map(c -> Map.of("id", c.getId(), "title", c.getTitle(), "content", String.valueOf(c.getContent()))).toList()),
                AiConflictPayload.class).ifPresent(payload -> payload.conflicts().forEach(item -> record(card, item)));
    }

    private void record(IdeaCard card, AiConflictPayload.Item item) {
        long a = Math.min(card.getId(), item.cardId());
        long b = Math.max(card.getId(), item.cardId());
        if (conflictRepository.findByCardAIdAndCardBId(a, b).isEmpty()) {
            conflictRepository.save(CardConflict.builder().roomId(card.getRoomId()).cardAId(a).cardBId(b).status(ConflictStatus.DETECTED).reason(item.reason()).detectedAt(KstTime.nowUtc()).build());
            realtime.toRoom(card.getRoomId(), "CONFLICT_CHANGED", Map.of("cardAId", a, "cardBId", b), null);
        }
    }
}
