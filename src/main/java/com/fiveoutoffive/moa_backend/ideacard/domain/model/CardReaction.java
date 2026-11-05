package com.fiveoutoffive.moa_backend.ideacard.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카드 이모지 리액션 (토글, 취소 시 행 삭제) (card_reaction)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardReaction {

    /** PK */
    private Long id;

    /** 카드 (NOT NULL) */
    private Long cardId;

    /** 반응한 멤버 (NOT NULL) */
    private Long accountId;

    /** 이모지 코드 (THUMBS_UP 등, 추가 가능성이 있어 CHECK 미적용) (NOT NULL) */
    private String emoji;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    private CardReaction(
            Long id,
            Long cardId,
            Long accountId,
            String emoji,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.cardId = cardId;
        this.accountId = accountId;
        this.emoji = emoji;
        this.createdAt = createdAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static CardReaction reconstitute(
            Long id,
            Long cardId,
            Long accountId,
            String emoji,
            LocalDateTime createdAt
    ) {
        return new CardReaction(id, cardId, accountId, emoji, createdAt);
    }
}
