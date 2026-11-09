package com.fiveoutoffive.moa_backend.notification.application.command;

import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationGroup;

public record GroupSettingCommand(NotificationGroup group, Boolean enabled) {
}
