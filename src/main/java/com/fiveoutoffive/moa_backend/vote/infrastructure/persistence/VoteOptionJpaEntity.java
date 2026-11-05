package com.fiveoutoffive.moa_backend.vote.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 투표 선택지 (vote_option). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "vote_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteOptionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "vote_id", nullable = false)
    private Long voteId;

    @Column(name = "content", nullable = false, length = 50)
    private String content;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public VoteOptionJpaEntity(
            Long id,
            Long voteId,
            String content,
            Integer sortOrder
    ) {
        this.id = id;
        this.voteId = voteId;
        this.content = content;
        this.sortOrder = sortOrder;
    }
}
