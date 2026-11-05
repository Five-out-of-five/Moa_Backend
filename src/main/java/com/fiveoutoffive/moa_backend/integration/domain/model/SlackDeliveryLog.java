package com.fiveoutoffive.moa_backend.integration.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 슬랙 발송 이력 (slack_delivery_log)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SlackDeliveryLog {

    /** PK */
    private Long id;

    /** 연동 (NOT NULL) */
    private Long integrationId;

    /** 발송 이벤트 (NOT NULL) */
    private SlackEventType eventType;

    /** 결과 (NOT NULL) */
    private SlackDeliveryStatus status;

    /** 재시도 횟수 (NOT NULL) */
    private Integer retryCount;

    /** 실패 사유 */
    private String errorMessage;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    private SlackDeliveryLog(
            Long id,
            Long integrationId,
            SlackEventType eventType,
            SlackDeliveryStatus status,
            Integer retryCount,
            String errorMessage,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.integrationId = integrationId;
        this.eventType = eventType;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static SlackDeliveryLog reconstitute(
            Long id,
            Long integrationId,
            SlackEventType eventType,
            SlackDeliveryStatus status,
            Integer retryCount,
            String errorMessage,
            LocalDateTime createdAt
    ) {
        return new SlackDeliveryLog(id, integrationId, eventType, status, retryCount, errorMessage, createdAt);
    }
}
