package com.fiveoutoffive.moa_backend.ai.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 요약 대상 회차 (여러 회차 지정) (ai_summary_session)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiSummarySession {

    /** 요약 */
    private Long summaryId;

    /** 회차 */
    private Long sessionId;

    private AiSummarySession(
            Long summaryId,
            Long sessionId
    ) {
        this.summaryId = summaryId;
        this.sessionId = sessionId;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static AiSummarySession reconstitute(
            Long summaryId,
            Long sessionId
    ) {
        return new AiSummarySession(summaryId, sessionId);
    }
}
