package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.ColumnTransformer;

/** 카드·업무·투표 임베딩 (pgvector) (content_embedding). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "content_embedding")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentEmbeddingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "card_id")
    private Long cardId;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "vote_id")
    private Long voteId;

    // pgvector 값은 '[0.1,0.2,...]' 문자열로 주고받는다. 유사도 검색은 어댑터에서 네이티브 쿼리(<=>)로 한다.
    @ColumnTransformer(write = "CAST(? AS vector)")
    @Column(name = "embedding", nullable = false, columnDefinition = "vector(768)")
    private String embedding;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Column(name = "source_hash", nullable = false, length = 64)
    private String sourceHash;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public ContentEmbeddingJpaEntity(
            Long id,
            Long roomId,
            Long cardId,
            Long taskId,
            Long voteId,
            String embedding,
            String model,
            String sourceHash,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.cardId = cardId;
        this.taskId = taskId;
        this.voteId = voteId;
        this.embedding = embedding;
        this.model = model;
        this.sourceHash = sourceHash;
        this.updatedAt = updatedAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
