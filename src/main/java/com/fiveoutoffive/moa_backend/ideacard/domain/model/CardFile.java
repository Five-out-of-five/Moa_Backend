package com.fiveoutoffive.moa_backend.ideacard.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카드 첨부 파일 (card_file)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardFile {

    /** PK */
    private Long id;

    /** 카드 (NOT NULL) */
    private Long cardId;

    /** 파일 (NOT NULL) */
    private Long fileId;

    /** 표시 순서 (카드당 최대 5개) (NOT NULL) */
    private Integer sortOrder;

    private CardFile(
            Long id,
            Long cardId,
            Long fileId,
            Integer sortOrder
    ) {
        this.id = id;
        this.cardId = cardId;
        this.fileId = fileId;
        this.sortOrder = sortOrder;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static CardFile reconstitute(
            Long id,
            Long cardId,
            Long fileId,
            Integer sortOrder
    ) {
        return new CardFile(id, cardId, fileId, sortOrder);
    }
}
