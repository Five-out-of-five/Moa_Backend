package com.fiveoutoffive.moa_backend.notification.application.result;

import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationGroup;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(Long notificationId, NotificationGroup group, NotificationType type, String title, String content, RoomInfo room, String targetType, Long targetId, Boolean read, LocalDateTime createdAt) {

    public record RoomInfo(Long roomId, String name) {
    }
}
