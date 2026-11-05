package com.fiveoutoffive.moa_backend.notification.domain.model;

/** 알림 그룹 (관리자·시스템은 해제 불가라 저장 대상 아님) (notification_setting.notification_group) */
public enum NotificationSettingGroup {
    MY_ACTIVITY,
    TASK,
    VOTE,
    SESSION,
    MEMBER_ROLE
}
