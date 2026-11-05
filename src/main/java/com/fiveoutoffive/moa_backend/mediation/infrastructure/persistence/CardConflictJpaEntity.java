package com.fiveoutoffive.moa_backend.mediation.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.mediation.domain.model.ConflictStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** AI 가 감지한 충돌 카드 쌍 (card_conflict). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "card_conflict")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardConflictJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "card_a_id", nullable = false)
    private Long cardAId;

    @Column(name = "card_b_id", nullable = false)
    private Long cardBId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConflictStatus status;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "dismissed_by_account_id")
    private Long dismissedByAccountId;

    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;

    @Column(name = "adopted_card_id")
    private Long adoptedCardId;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public CardConflictJpaEntity(
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

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.status == null) {
            this.status = ConflictStatus.DETECTED;
        }
        if (this.detectedAt == null) {
            this.detectedAt = now;
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
