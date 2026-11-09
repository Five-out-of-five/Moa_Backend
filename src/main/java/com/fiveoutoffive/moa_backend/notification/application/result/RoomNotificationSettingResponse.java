package com.fiveoutoffive.moa_backend.notification.application.result;

import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationGroup;
import java.util.List;

public record RoomNotificationSettingResponse(boolean muted, List<GroupSetting> groups) {

    public record GroupSetting(NotificationGroup group, Boolean enabled, boolean effective) {
    }
}
