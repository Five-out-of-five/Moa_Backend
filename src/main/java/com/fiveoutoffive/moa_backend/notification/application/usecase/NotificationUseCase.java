package com.fiveoutoffive.moa_backend.notification.application.usecase;

import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.notification.application.command.GroupSettingCommand;
import com.fiveoutoffive.moa_backend.notification.application.result.NotificationResponse;
import com.fiveoutoffive.moa_backend.notification.application.result.NotificationSettingResponse;
import com.fiveoutoffive.moa_backend.notification.application.result.RoomNotificationSettingResponse;
import com.fiveoutoffive.moa_backend.notification.application.result.UnreadCountResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface NotificationUseCase {

    PageResponse<NotificationResponse> getList(Long accountId, boolean unreadOnly, Pageable pageable);

    UnreadCountResponse getUnreadCount(Long accountId);

    void markRead(Long accountId, Long notificationId);

    void markAllRead(Long accountId);

    void delete(Long accountId, Long notificationId);

    NotificationSettingResponse getGlobalSettings(Long accountId);

    NotificationSettingResponse updateGlobalSettings(Long accountId, List<GroupSettingCommand> groups);

    RoomNotificationSettingResponse getRoomSettings(Long accountId, Long roomId);

    RoomNotificationSettingResponse updateRoomSettings(Long accountId, Long roomId, boolean muted, List<GroupSettingCommand> groups);
}
