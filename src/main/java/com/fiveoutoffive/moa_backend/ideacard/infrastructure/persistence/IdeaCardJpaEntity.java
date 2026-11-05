package com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;
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
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 아이디어 보드 카드 (안건 / KPT) (idea_card). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "idea_card")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdeaCardJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false, length = 20)
    private CardType cardType;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", length = 1000)
    private String content;

    @Column(name = "writer_account_id", nullable = false)
    private Long writerAccountId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_ai_generated", nullable = false)
    private Boolean isAiGenerated;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public IdeaCardJpaEntity(
            Long id,
            Long roomId,
            Long sessionId,
            CardType cardType,
            String title,
            String content,
            Long writerAccountId,
            Integer sortOrder,
            Boolean isAiGenerated,
            Long version,
            LocalDateTime editedAt,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.sessionId = sessionId;
        this.cardType = cardType;
        this.title = title;
        this.content = content;
        this.writerAccountId = writerAccountId;
        this.sortOrder = sortOrder;
        this.isAiGenerated = isAiGenerated;
        this.version = version;
        this.editedAt = editedAt;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
        if (this.isAiGenerated == null) {
            this.isAiGenerated = Boolean.FALSE;
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
