package com.fiveoutoffive.moa_backend.mediation.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.mediation.domain.model.DraftCardType;
import com.fiveoutoffive.moa_backend.mediation.domain.model.DraftStatus;
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

/** AI 절충안 초안 (채택 전에는 요청자에게만 노출) (compromise_draft). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "compromise_draft")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompromiseDraftJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "conflict_id", nullable = false)
    private Long conflictId;

    @Column(name = "requested_by_account_id", nullable = false)
    private Long requestedByAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false, length = 20)
    private DraftCardType cardType;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DraftStatus status;

    @Column(name = "adopted_card_id")
    private Long adoptedCardId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public CompromiseDraftJpaEntity(
            Long id,
            Long conflictId,
            Long requestedByAccountId,
            DraftCardType cardType,
            String title,
            String content,
            DraftStatus status,
            Long adoptedCardId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.conflictId = conflictId;
        this.requestedByAccountId = requestedByAccountId;
        this.cardType = cardType;
        this.title = title;
        this.content = content;
        this.status = status;
        this.adoptedCardId = adoptedCardId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.status == null) {
            this.status = DraftStatus.DRAFT;
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
