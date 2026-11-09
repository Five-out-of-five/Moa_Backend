package com.fiveoutoffive.moa_backend.notification.presentation.api.request;

import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationGroup;
import jakarta.validation.constraints.NotNull;

public record GroupSettingRequest(@NotNull NotificationGroup group, Boolean enabled) {
}
