package com.fiveoutoffive.moa_backend.vote.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 투표 참여 내역 (변경 시 덮어씀) (vote_ballot)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteBallot {

    /** PK */
    private Long id;

    /** 투표 (NOT NULL) */
    private Long voteId;

    /** 선택지 (복합 FK 로 같은 투표의 선택지만 허용) (NOT NULL) */
    private Long optionId;

    /** 투표자 (익명 투표도 저장, 화면 비노출) (NOT NULL) */
    private Long accountId;

    /** 투표 시각 (NOT NULL) */
    private LocalDateTime votedAt;

    private VoteBallot(
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

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static VoteBallot reconstitute(
            Long id,
            Long voteId,
            Long optionId,
            Long accountId,
            LocalDateTime votedAt
    ) {
        return new VoteBallot(id, voteId, optionId, accountId, votedAt);
    }
}
