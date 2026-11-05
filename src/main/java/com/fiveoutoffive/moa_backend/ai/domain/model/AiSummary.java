package com.fiveoutoffive.moa_backend.ai.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 회의 요약 (ai_summary)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiSummary {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 요약 범위: 보드 전체 / 지정한 회차 (NOT NULL) */
    private SummaryScopeType scopeType;

    /** 요약 결과 (초안, 수정 가능) (NOT NULL) */
    private String content;

    /** 요청한 멤버 (NOT NULL) */
    private Long requestedByAccountId;

    /** 카드로 저장한 경우 해당 카드 */
    private Long savedCardId;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private AiSummary(
            Long id,
            Long roomId,
            SummaryScopeType scopeType,
            String content,
            Long requestedByAccountId,
            Long savedCardId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.scopeType = scopeType;
        this.content = content;
        this.requestedByAccountId = requestedByAccountId;
        this.savedCardId = savedCardId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static AiSummary reconstitute(
            Long id,
            Long roomId,
            SummaryScopeType scopeType,
            String content,
            Long requestedByAccountId,
            Long savedCardId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new AiSummary(id, roomId, scopeType, content, requestedByAccountId, savedCardId, createdAt, updatedAt);
    }
}
