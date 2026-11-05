package com.fiveoutoffive.moa_backend.ai.domain.model;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 기능별 일일 사용량 (방별 호출 상한 관리) (ai_usage_quota)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiUsageQuota {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** AI 기능 (NOT NULL) */
    private AiFeature feature;

    /** 일자 (NOT NULL) */
    private LocalDate quotaDate;

    /** 사용 횟수 (NOT NULL) */
    private Integer usedCount;

    private AiUsageQuota(
            Long id,
            Long roomId,
            AiFeature feature,
            LocalDate quotaDate,
            Integer usedCount
    ) {
        this.id = id;
        this.roomId = roomId;
        this.feature = feature;
        this.quotaDate = quotaDate;
        this.usedCount = usedCount;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static AiUsageQuota reconstitute(
            Long id,
            Long roomId,
            AiFeature feature,
            LocalDate quotaDate,
            Integer usedCount
    ) {
        return new AiUsageQuota(id, roomId, feature, quotaDate, usedCount);
    }
}
