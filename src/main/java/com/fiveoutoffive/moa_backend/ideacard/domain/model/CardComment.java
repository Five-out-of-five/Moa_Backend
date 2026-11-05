package com.fiveoutoffive.moa_backend.ideacard.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카드 세부 의견(댓글) (card_comment)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardComment {

    /** PK */
    private Long id;

    /** 카드 (NOT NULL) */
    private Long cardId;

    /** 작성자 (NOT NULL) */
    private Long writerAccountId;

    /** 내용 (NOT NULL) */
    private String content;

    /** 수정 시각 ('수정됨' 표시) */
    private LocalDateTime editedAt;

    /** 소프트 딜리트 시각 (NULL = 정상) */
    private LocalDateTime deletedAt;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    private CardComment(
            Long id,
            Long cardId,
            Long writerAccountId,
            String content,
            LocalDateTime editedAt,
            LocalDateTime deletedAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.cardId = cardId;
        this.writerAccountId = writerAccountId;
        this.content = content;
        this.editedAt = editedAt;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static CardComment reconstitute(
            Long id,
            Long cardId,
            Long writerAccountId,
            String content,
            LocalDateTime editedAt,
            LocalDateTime deletedAt,
            LocalDateTime createdAt
    ) {
        return new CardComment(id, cardId, writerAccountId, content, editedAt, deletedAt, createdAt);
    }
}
