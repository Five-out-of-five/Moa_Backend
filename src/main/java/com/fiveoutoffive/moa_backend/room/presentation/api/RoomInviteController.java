package com.fiveoutoffive.moa_backend.room.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.room.application.result.InviteLinkResponse;
import com.fiveoutoffive.moa_backend.room.application.result.InvitePreviewResponse;
import com.fiveoutoffive.moa_backend.room.application.result.JoinRequestResponse;
import com.fiveoutoffive.moa_backend.room.application.usecase.RoomUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "04. Room", description = "방")
public class RoomInviteController {

    private final RoomUseCase roomUseCase;

    @GetMapping("/rooms/{roomId}/invite-link")
    @Operation(summary = "초대 링크 조회")
    public ApiResponse<InviteLinkResponse> getInviteLink(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(roomUseCase.getInviteLink(accountId, roomId));
    }

    @GetMapping("/invites/{inviteCode}")
    @Operation(summary = "초대 링크로 방 정보 확인")
    public ApiResponse<InvitePreviewResponse> preview(@LoginAccount Long accountId, @PathVariable String inviteCode) {
        return ApiResponse.success(roomUseCase.previewInvite(accountId, inviteCode));
    }

    @PostMapping("/invites/{inviteCode}/join-requests")
    @Operation(summary = "참여 요청")
    public ApiResponse<JoinRequestResponse> requestJoin(@LoginAccount Long accountId, @PathVariable String inviteCode) {
        return ApiResponse.success(roomUseCase.requestJoin(accountId, inviteCode));
    }
}
