package com.fiveoutoffive.moa_backend.vote.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.vote.domain.model.VoteCloseReason;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteType;
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

/** 투표 (vote). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "vote")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "source_card_id")
    private Long sourceCardId;

    @Column(name = "parent_vote_id")
    private Long parentVoteId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false, length = 20)
    private VoteType voteType;

    @Column(name = "max_select_count", nullable = false)
    private Integer maxSelectCount;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VoteStatus status;

    @Column(name = "deadline_at", nullable = false)
    private LocalDateTime deadlineAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "close_reason", length = 20)
    private VoteCloseReason closeReason;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "closed_by_account_id")
    private Long closedByAccountId;

    @Column(name = "creator_account_id", nullable = false)
    private Long creatorAccountId;

    @Column(name = "reminder_sent_at")
    private LocalDateTime reminderSentAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public VoteJpaEntity(
            Long id,
            Long roomId,
            Long sourceCardId,
            Long parentVoteId,
            String title,
            VoteType voteType,
            Integer maxSelectCount,
            Boolean isAnonymous,
            VoteStatus status,
            LocalDateTime deadlineAt,
            VoteCloseReason closeReason,
            LocalDateTime closedAt,
            Long closedByAccountId,
            Long creatorAccountId,
            LocalDateTime reminderSentAt,
            Long version,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.sourceCardId = sourceCardId;
        this.parentVoteId = parentVoteId;
        this.title = title;
        this.voteType = voteType;
        this.maxSelectCount = maxSelectCount;
        this.isAnonymous = isAnonymous;
        this.status = status;
        this.deadlineAt = deadlineAt;
        this.closeReason = closeReason;
        this.closedAt = closedAt;
        this.closedByAccountId = closedByAccountId;
        this.creatorAccountId = creatorAccountId;
        this.reminderSentAt = reminderSentAt;
        this.version = version;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.maxSelectCount == null) {
            this.maxSelectCount = 1;
        }
        if (this.isAnonymous == null) {
            this.isAnonymous = Boolean.FALSE;
        }
        if (this.status == null) {
            this.status = VoteStatus.OPEN;
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
