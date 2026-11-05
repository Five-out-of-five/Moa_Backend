package com.fiveoutoffive.moa_backend.notification.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 (헤더 전역 통합, 90일 보관) (notification)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    /** PK */
    private Long id;

    /** 수신자 (NOT NULL) */
    private Long ownerAccountId;

    /** 발생한 방 (시스템 알림은 NULL) */
    private Long roomId;

    /** 알림 그룹 (내 활동/업무/투표/회차/멤버·권한/관리자·시스템) (NOT NULL) */
    private NotificationGroup notificationGroup;

    /** 알림 유형 (27종) (NOT NULL) */
    private NotificationType notificationType;

    /** 제목 (NOT NULL) */
    private String title;

    /** 내용 */
    private String content;

    /** 이동 대상 종류 */
    private String targetType;

    /** 이동 대상 ID */
    private Long targetId;

    /** 읽음 여부 (NOT NULL) */
    private Boolean read;

    /** 읽은 시각 */
    private LocalDateTime readAt;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    private Notification(
            Long id,
            Long ownerAccountId,
            Long roomId,
            NotificationGroup notificationGroup,
            NotificationType notificationType,
            String title,
            String content,
            String targetType,
            Long targetId,
            Boolean read,
            LocalDateTime readAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.ownerAccountId = ownerAccountId;
        this.roomId = roomId;
        this.notificationGroup = notificationGroup;
        this.notificationType = notificationType;
        this.title = title;
        this.content = content;
        this.targetType = targetType;
        this.targetId = targetId;
        this.read = read;
        this.readAt = readAt;
        this.createdAt = createdAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static Notification reconstitute(
            Long id,
            Long ownerAccountId,
            Long roomId,
            NotificationGroup notificationGroup,
            NotificationType notificationType,
            String title,
            String content,
            String targetType,
            Long targetId,
            Boolean read,
            LocalDateTime readAt,
            LocalDateTime createdAt
    ) {
        return new Notification(id, ownerAccountId, roomId, notificationGroup, notificationType, title, content, targetType, targetId, read, readAt, createdAt);
    }
}
