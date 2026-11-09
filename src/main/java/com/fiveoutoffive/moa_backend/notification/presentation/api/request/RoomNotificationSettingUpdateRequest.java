package com.fiveoutoffive.moa_backend.notification.presentation.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RoomNotificationSettingUpdateRequest(@NotNull Boolean muted, @Valid List<GroupSettingRequest> groups) {
}
