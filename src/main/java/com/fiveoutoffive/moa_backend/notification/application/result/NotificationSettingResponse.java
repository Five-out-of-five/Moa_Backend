package com.fiveoutoffive.moa_backend.notification.application.result;

import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationGroup;
import java.util.List;

public record NotificationSettingResponse(List<GroupSetting> groups) {

    public record GroupSetting(NotificationGroup group, boolean enabled, boolean locked) {
    }
}
