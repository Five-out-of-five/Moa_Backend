package com.fiveoutoffive.moa_backend.mediation.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 절충안 초안 (채택 전에는 요청자에게만 노출) (compromise_draft)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompromiseDraft {

    /** PK */
    private Long id;

    /** 충돌 쌍 (NOT NULL) */
    private Long conflictId;

    /** 절충안을 요청한 멤버 (NOT NULL) */
    private Long requestedByAccountId;

    /** 제안 카드 유형 (원본이 안건이면 AGENDA, 그 외 TRY) (NOT NULL) */
    private DraftCardType cardType;

    /** 제목 (NOT NULL) */
    private String title;

    /** 내용 */
    private String content;

    /** 초안 / 채택 / 폐기 (NOT NULL) */
    private DraftStatus status;

    /** 채택 시 생성된 카드 */
    private Long adoptedCardId;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private CompromiseDraft(
            Long id,
            Long conflictId,
            Long requestedByAccountId,
            DraftCardType cardType,
            String title,
            String content,
            DraftStatus status,
            Long adoptedCardId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.conflictId = conflictId;
        this.requestedByAccountId = requestedByAccountId;
        this.cardType = cardType;
        this.title = title;
        this.content = content;
        this.status = status;
        this.adoptedCardId = adoptedCardId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static CompromiseDraft reconstitute(
            Long id,
            Long conflictId,
            Long requestedByAccountId,
            DraftCardType cardType,
            String title,
            String content,
            DraftStatus status,
            Long adoptedCardId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new CompromiseDraft(id, conflictId, requestedByAccountId, cardType, title, content, status, adoptedCardId, createdAt, updatedAt);
    }
}
