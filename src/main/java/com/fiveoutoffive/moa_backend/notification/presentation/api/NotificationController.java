package com.fiveoutoffive.moa_backend.notification.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.notification.application.result.NotificationResponse;
import com.fiveoutoffive.moa_backend.notification.application.result.UnreadCountResponse;
import com.fiveoutoffive.moa_backend.notification.application.usecase.NotificationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "14. Notification", description = "알림")
public class NotificationController {

    private final NotificationUseCase useCase;

    @GetMapping
    @Operation(summary = "알림 목록")
    public ApiResponse<PageResponse<NotificationResponse>> list(@LoginAccount Long accountId, @RequestParam(defaultValue = "false") boolean unreadOnly,
                                                                @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(useCase.getList(accountId, unreadOnly, Pages.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "안읽은 알림 수")
    public ApiResponse<UnreadCountResponse> unreadCount(@LoginAccount Long accountId) {
        return ApiResponse.success(useCase.getUnreadCount(accountId));
    }

    @PatchMapping("/{notificationId:\\d+}/read")
    @Operation(summary = "알림 읽음 처리")
    public ApiResponse<Void> markRead(@LoginAccount Long accountId, @PathVariable Long notificationId) {
        useCase.markRead(accountId, notificationId);
        return ApiResponse.empty();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "알림 모두 읽음")
    public ApiResponse<Void> markAllRead(@LoginAccount Long accountId) {
        useCase.markAllRead(accountId);
        return ApiResponse.empty();
    }

    @DeleteMapping("/{notificationId:\\d+}")
    @Operation(summary = "알림 삭제")
    public ApiResponse<Void> delete(@LoginAccount Long accountId, @PathVariable Long notificationId) {
        useCase.delete(accountId, notificationId);
        return ApiResponse.empty();
    }
}
