package com.fiveoutoffive.moa_backend.room.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.room.application.result.JoinRequestResponse;
import com.fiveoutoffive.moa_backend.room.application.usecase.RoomUseCase;
import com.fiveoutoffive.moa_backend.room.domain.model.JoinRequestStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/join-requests")
@RequiredArgsConstructor
@Tag(name = "04. Room", description = "방")
public class JoinRequestController {

    private final RoomUseCase roomUseCase;

    @GetMapping
    @Operation(summary = "참여 요청 목록")
    public ApiResponse<PageResponse<JoinRequestResponse>> getList(@LoginAccount Long accountId, @PathVariable Long roomId,
                                                                  @RequestParam(defaultValue = "PENDING") JoinRequestStatus status,
                                                                  @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(roomUseCase.getJoinRequests(accountId, roomId, status, Pages.of(page, size, Sort.by(Sort.Direction.DESC, "requestedAt"))));
    }

    @PostMapping("/{requestId}/approve")
    @Operation(summary = "참여 요청 승인")
    public ApiResponse<JoinRequestResponse> approve(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long requestId) {
        return ApiResponse.success(roomUseCase.approve(accountId, roomId, requestId));
    }

    @PostMapping("/{requestId}/reject")
    @Operation(summary = "참여 요청 거절")
    public ApiResponse<JoinRequestResponse> reject(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long requestId) {
        return ApiResponse.success(roomUseCase.reject(accountId, roomId, requestId));
    }
}
