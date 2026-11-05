package com.fiveoutoffive.moa_backend.mediation.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 가 감지한 충돌 카드 쌍 (card_conflict)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardConflict {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 충돌 카드 (작은 ID) (NOT NULL) */
    private Long cardAId;

    /** 충돌 카드 (큰 ID) (NOT NULL) */
    private Long cardBId;

    /** 감지됨 / 해제됨 / 절충안 제안됨 / 절충안 폐기 후 재감지 (NOT NULL) */
    private ConflictStatus status;

    /** AI 판정 근거 */
    private String reason;

    /** 배지를 해제한 멤버 (해제 권한은 팀 상의 중) */
    private Long dismissedByAccountId;

    /** 해제 시각 (해제된 쌍은 재감지하지 않음) */
    private LocalDateTime dismissedAt;

    /** 채택된 절충안 카드 */
    private Long adoptedCardId;

    /** 감지 시각 (NOT NULL) */
    private LocalDateTime detectedAt;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private CardConflict(
            Long id,
            Long roomId,
            Long cardAId,
            Long cardBId,
            ConflictStatus status,
            String reason,
            Long dismissedByAccountId,
            LocalDateTime dismissedAt,
            Long adoptedCardId,
            LocalDateTime detectedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.cardAId = cardAId;
        this.cardBId = cardBId;
        this.status = status;
        this.reason = reason;
        this.dismissedByAccountId = dismissedByAccountId;
        this.dismissedAt = dismissedAt;
        this.adoptedCardId = adoptedCardId;
        this.detectedAt = detectedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static CardConflict reconstitute(
            Long id,
            Long roomId,
            Long cardAId,
            Long cardBId,
            ConflictStatus status,
            String reason,
            Long dismissedByAccountId,
            LocalDateTime dismissedAt,
            Long adoptedCardId,
            LocalDateTime detectedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new CardConflict(id, roomId, cardAId, cardBId, status, reason, dismissedByAccountId, dismissedAt, adoptedCardId, detectedAt, createdAt, updatedAt);
    }
}
