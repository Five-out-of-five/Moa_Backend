package com.fiveoutoffive.moa_backend.notification.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 수신 설정 (room_id NULL = 전역) (notification_setting)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting {

    /** PK */
    private Long id;

    /** 회원 (NOT NULL) */
    private Long accountId;

    /** 방 (NULL = 전역 설정) */
    private Long roomId;

    /** 알림 그룹 (관리자·시스템은 해제 불가라 저장 대상 아님) (NOT NULL) */
    private NotificationSettingGroup notificationGroup;

    /** 수신 여부 (NOT NULL) */
    private Boolean enabled;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private NotificationSetting(
            Long id,
            Long accountId,
            Long roomId,
            NotificationSettingGroup notificationGroup,
            Boolean enabled,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.accountId = accountId;
        this.roomId = roomId;
        this.notificationGroup = notificationGroup;
        this.enabled = enabled;
        this.updatedAt = updatedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static NotificationSetting reconstitute(
            Long id,
            Long accountId,
            Long roomId,
            NotificationSettingGroup notificationGroup,
            Boolean enabled,
            LocalDateTime updatedAt
    ) {
        return new NotificationSetting(id, accountId, roomId, notificationGroup, enabled, updatedAt);
    }
}
