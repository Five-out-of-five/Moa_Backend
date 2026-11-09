package com.fiveoutoffive.moa_backend.notification.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.notification.application.command.GroupSettingCommand;
import com.fiveoutoffive.moa_backend.notification.application.result.NotificationSettingResponse;
import com.fiveoutoffive.moa_backend.notification.application.result.RoomNotificationSettingResponse;
import com.fiveoutoffive.moa_backend.notification.application.usecase.NotificationUseCase;
import com.fiveoutoffive.moa_backend.notification.presentation.api.request.GroupSettingRequest;
import com.fiveoutoffive.moa_backend.notification.presentation.api.request.NotificationSettingUpdateRequest;
import com.fiveoutoffive.moa_backend.notification.presentation.api.request.RoomNotificationSettingUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "14. Notification", description = "알림")
public class NotificationSettingController {

    private final NotificationUseCase useCase;

    @GetMapping("/notification-settings")
    @Operation(summary = "전역 알림 설정 조회")
    public ApiResponse<NotificationSettingResponse> getGlobal(@LoginAccount Long accountId) {
        return ApiResponse.success(useCase.getGlobalSettings(accountId));
    }

    @PutMapping("/notification-settings")
    @Operation(summary = "전역 알림 설정 변경")
    public ApiResponse<NotificationSettingResponse> updateGlobal(@LoginAccount Long accountId, @Valid @RequestBody NotificationSettingUpdateRequest request) {
        return ApiResponse.success(useCase.updateGlobalSettings(accountId, toCommands(request.groups())));
    }

    @GetMapping("/rooms/{roomId}/notification-settings")
    @Operation(summary = "방별 알림 설정 조회")
    public ApiResponse<RoomNotificationSettingResponse> getRoom(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.getRoomSettings(accountId, roomId));
    }

    @PutMapping("/rooms/{roomId}/notification-settings")
    @Operation(summary = "방별 알림 설정 변경")
    public ApiResponse<RoomNotificationSettingResponse> updateRoom(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody RoomNotificationSettingUpdateRequest request) {
        return ApiResponse.success(useCase.updateRoomSettings(accountId, roomId, request.muted(), request.groups() == null ? null : toCommands(request.groups())));
    }

    private List<GroupSettingCommand> toCommands(List<GroupSettingRequest> groups) {
        return groups.stream().map(g -> new GroupSettingCommand(g.group(), g.enabled())).toList();
    }
}
