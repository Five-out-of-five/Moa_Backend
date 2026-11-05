package com.fiveoutoffive.moa_backend.vote.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 투표 참여 내역 (변경 시 덮어씀) (vote_ballot). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "vote_ballot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteBallotJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "vote_id", nullable = false)
    private Long voteId;

    @Column(name = "option_id", nullable = false)
    private Long optionId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "voted_at", nullable = false)
    private LocalDateTime votedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public VoteBallotJpaEntity(
            Long id,
            Long voteId,
            Long optionId,
            Long accountId,
            LocalDateTime votedAt
    ) {
        this.id = id;
        this.voteId = voteId;
        this.optionId = optionId;
        this.accountId = accountId;
        this.votedAt = votedAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.votedAt == null) {
            this.votedAt = now;
        }
    }
}
