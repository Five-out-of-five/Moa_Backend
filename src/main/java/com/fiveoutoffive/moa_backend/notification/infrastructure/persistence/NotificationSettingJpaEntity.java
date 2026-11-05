package com.fiveoutoffive.moa_backend.notification.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationSettingGroup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 알림 수신 설정 (room_id NULL = 전역) (notification_setting). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "notification_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSettingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "room_id")
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_group", nullable = false, length = 20)
    private NotificationSettingGroup notificationGroup;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public NotificationSettingJpaEntity(
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

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
