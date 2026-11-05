package com.fiveoutoffive.moa_backend.integration.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 슬랙 Incoming Webhook 연동 (방당 1개) (slack_integration)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SlackIntegration {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 암호화된 Webhook URL (NOT NULL) */
    private byte[] webhookUrlEnc;

    /** 발송 이벤트 목록 VOTE_CREATED/VOTE_CLOSED/TASK_DUE_SOON/SESSION_SUMMARY (NOT NULL) */
    private String[] eventTypes;

    /** 활성 여부 (NOT NULL) */
    private Boolean enabled;

    /** 연속 실패 횟수 (임계 초과 시 자동 비활성) (NOT NULL) */
    private Integer consecutiveFailCount;

    /** 자동 비활성 시각 */
    private LocalDateTime disabledAt;

    /** 등록한 방장 (NOT NULL) */
    private Long createdByAccountId;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private SlackIntegration(
            Long id,
            Long roomId,
            byte[] webhookUrlEnc,
            String[] eventTypes,
            Boolean enabled,
            Integer consecutiveFailCount,
            LocalDateTime disabledAt,
            Long createdByAccountId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.webhookUrlEnc = webhookUrlEnc;
        this.eventTypes = eventTypes;
        this.enabled = enabled;
        this.consecutiveFailCount = consecutiveFailCount;
        this.disabledAt = disabledAt;
        this.createdByAccountId = createdByAccountId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static SlackIntegration reconstitute(
            Long id,
            Long roomId,
            byte[] webhookUrlEnc,
            String[] eventTypes,
            Boolean enabled,
            Integer consecutiveFailCount,
            LocalDateTime disabledAt,
            Long createdByAccountId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new SlackIntegration(id, roomId, webhookUrlEnc, eventTypes, enabled, consecutiveFailCount, disabledAt, createdByAccountId, createdAt, updatedAt);
    }
}
