package com.fiveoutoffive.moa_backend.vote.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 투표 선택지 (vote_option)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteOption {

    /** PK */
    private Long id;

    /** 투표 (NOT NULL) */
    private Long voteId;

    /** 선택지 내용 (NOT NULL) */
    private String content;

    /** 표시 순서 (최대 10개) (NOT NULL) */
    private Integer sortOrder;

    private VoteOption(
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

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static VoteOption reconstitute(
            Long id,
            Long voteId,
            String content,
            Integer sortOrder
    ) {
        return new VoteOption(id, voteId, content, sortOrder);
    }
}
