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
import com.fiveoutoffive.moa_backend.mediation.domain.repository.CardConflictRepository;
import com.fiveoutoffive.moa_backend.session.domain.event.SessionClosedEvent;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 카드 임베딩 갱신과 AI 충돌 감지. 커밋 후 비동기로 돌며, AI 서버가 없어도 사용자 요청에는 영향이 없다.
 * 해제된 쌍은 다시 감지하지 않는다.
 *
 * <p>핸들러에는 트랜잭션을 걸지 않는다. AI 응답을 기다리는 동안 DB 연결을 붙잡지 않고, 임베딩과 충돌 쌍을
 * 각각 따로 커밋해서 한쪽 실패(예: 동시에 같은 쌍 저장)가 다른 쪽을 되돌리지 않게 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConflictDetectionHandlers {

    private static final int NEAREST_LIMIT = 10;
    // card_conflict.reason VARCHAR(500)
    private static final int REASON_LIMIT = 500;

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
                AiConflictPayload.class).ifPresent(payload -> {
                    // AI 가 준 ID 는 이번에 보낸 후보(같은 방, 삭제 안 됨) 안에 있을 때만 믿는다. 지어낸 ID 나 자기 자신은 버린다.
                    Set<Long> candidateIds = candidates.stream().filter(c -> c.getRoomId().equals(card.getRoomId())).map(IdeaCard::getId).collect(Collectors.toSet());
                    if (payload.conflicts() == null) {
                        return;
                    }
                    payload.conflicts().stream()
                            .filter(item -> item != null && item.cardId() != null && !item.cardId().equals(card.getId()) && candidateIds.contains(item.cardId()))
                            .forEach(item -> record(card, item));
                });
    }

    private void record(IdeaCard card, AiConflictPayload.Item item) {
        long a = Math.min(card.getId(), item.cardId());
        long b = Math.max(card.getId(), item.cardId());
        try {
            if (conflictRepository.insertIfAbsent(card.getRoomId(), a, b, clip(item.reason()), KstTime.nowUtc())) {
                realtime.toRoom(card.getRoomId(), "CONFLICT_CHANGED", Map.of("cardAId", a, "cardBId", b), null);
            }
        } catch (Exception e) {
            // 그 사이 카드가 지워진 경우 등. 이 쌍만 건너뛰고 나머지는 계속 저장한다.
            log.warn("[AI] 충돌 저장 실패 cardAId={} cardBId={}: {}", a, b, e.getMessage());
        }
    }

    private String clip(String reason) {
        return reason == null || reason.length() <= REASON_LIMIT ? reason : reason.substring(0, REASON_LIMIT);
    }
}
