package com.fiveoutoffive.moa_backend.notification.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationGroup;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 알림 (헤더 전역 통합, 90일 보관) (notification). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "owner_account_id", nullable = false)
    private Long ownerAccountId;

    @Column(name = "room_id")
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_group", nullable = false, length = 20)
    private NotificationGroup notificationGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", length = 500)
    private String content;

    @Column(name = "target_type", length = 20)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "read", nullable = false)
    private Boolean read;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public NotificationJpaEntity(
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

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.read == null) {
            this.read = Boolean.FALSE;
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }
}
