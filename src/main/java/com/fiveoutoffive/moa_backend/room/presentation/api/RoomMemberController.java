package com.fiveoutoffive.moa_backend.room.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.room.application.result.LeaveCheckResponse;
import com.fiveoutoffive.moa_backend.room.application.result.MemberProfileResponse;
import com.fiveoutoffive.moa_backend.room.application.result.MemberResponse;
import com.fiveoutoffive.moa_backend.room.application.usecase.RoomUseCase;
import com.fiveoutoffive.moa_backend.room.presentation.api.request.MemberProfileUpdateRequest;
import com.fiveoutoffive.moa_backend.room.presentation.api.request.OwnerDelegationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}")
@RequiredArgsConstructor
@Tag(name = "04. Room", description = "방")
public class RoomMemberController {

    private final RoomUseCase roomUseCase;

    @GetMapping("/members")
    @Operation(summary = "멤버 목록")
    public ApiResponse<List<MemberResponse>> getMembers(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(roomUseCase.getMembers(accountId, roomId));
    }

    @GetMapping("/members/{targetId:\\d+}")
    @Operation(summary = "멤버 프로필 조회")
    public ApiResponse<MemberProfileResponse> getProfile(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long targetId) {
        return ApiResponse.success(roomUseCase.getMemberProfile(accountId, roomId, targetId));
    }

    @PatchMapping("/members/me/profile")
    @Operation(summary = "방별 프로필 설정")
    public ApiResponse<MemberProfileResponse> updateProfile(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody MemberProfileUpdateRequest request) {
        return ApiResponse.success(roomUseCase.updateMyProfile(accountId, roomId, request.toCommand()));
    }

    @GetMapping("/members/me/leave-check")
    @Operation(summary = "방 나가기 확인")
    public ApiResponse<LeaveCheckResponse> checkLeave(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(roomUseCase.checkLeave(accountId, roomId));
    }

    @DeleteMapping("/members/me")
    @Operation(summary = "방 나가기")
    public ApiResponse<Void> leave(@LoginAccount Long accountId, @PathVariable Long roomId) {
        roomUseCase.leave(accountId, roomId);
        return ApiResponse.empty();
    }

    @DeleteMapping("/members/{targetId:\\d+}")
    @Operation(summary = "멤버 내보내기")
    public ApiResponse<Void> kick(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long targetId) {
        roomUseCase.kick(accountId, roomId, targetId);
        return ApiResponse.empty();
    }

    @PostMapping("/delegate-owner")
    @Operation(summary = "방장 위임")
    public ApiResponse<List<MemberResponse>> delegate(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody OwnerDelegationRequest request) {
        return ApiResponse.success(roomUseCase.delegateOwner(accountId, roomId, request.toCommand()));
    }
}
