package com.fiveoutoffive.moa_backend.chat.presentation.api;

import com.fiveoutoffive.moa_backend.chat.application.result.ChannelMemberResponse;
import com.fiveoutoffive.moa_backend.chat.application.result.ChannelResponse;
import com.fiveoutoffive.moa_backend.chat.application.usecase.ChatUseCase;
import com.fiveoutoffive.moa_backend.chat.presentation.api.request.ChannelCreateRequest;
import com.fiveoutoffive.moa_backend.chat.presentation.api.request.ChannelDeleteRequest;
import com.fiveoutoffive.moa_backend.chat.presentation.api.request.ChannelMemberAddRequest;
import com.fiveoutoffive.moa_backend.chat.presentation.api.request.ChannelUpdateRequest;
import com.fiveoutoffive.moa_backend.chat.presentation.api.request.DirectChannelRequest;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
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
@Tag(name = "13. Chat", description = "채팅")
public class ChannelController {

    private final ChatUseCase useCase;

    @GetMapping("/channels")
    @Operation(summary = "채널 목록 (사이드바)")
    public ApiResponse<List<ChannelResponse>> list(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.getChannels(accountId, roomId));
    }

    @PostMapping("/channels")
    @Operation(summary = "채널 생성")
    public ApiResponse<ChannelResponse> create(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody ChannelCreateRequest request) {
        return ApiResponse.success(useCase.create(accountId, roomId, request.toCommand()));
    }

    @GetMapping("/channels/{channelId}")
    @Operation(summary = "채널 상세")
    public ApiResponse<ChannelResponse> detail(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long channelId) {
        return ApiResponse.success(useCase.getChannel(accountId, roomId, channelId));
    }

    @PatchMapping("/channels/{channelId}")
    @Operation(summary = "채널 수정")
    public ApiResponse<ChannelResponse> update(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long channelId, @Valid @RequestBody ChannelUpdateRequest request) {
        return ApiResponse.success(useCase.update(accountId, roomId, channelId, request.toCommand()));
    }

    @DeleteMapping("/channels/{channelId}")
    @Operation(summary = "채널 삭제")
    public ApiResponse<Void> delete(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long channelId, @Valid @RequestBody ChannelDeleteRequest request) {
        useCase.delete(accountId, roomId, channelId, request.toCommand());
        return ApiResponse.empty();
    }

    @PostMapping("/channels/{channelId}/join")
    @Operation(summary = "공개 채널 참여")
    public ApiResponse<ChannelResponse> join(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long channelId) {
        return ApiResponse.success(useCase.joinPublic(accountId, roomId, channelId));
    }

    @PostMapping("/channels/{channelId}/leave")
    @Operation(summary = "채널 나가기")
    public ApiResponse<Void> leave(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long channelId) {
        useCase.leave(accountId, roomId, channelId);
        return ApiResponse.empty();
    }

    @PostMapping("/channels/{channelId}/accept-invitation")
    @Operation(summary = "채널 초대 수락")
    public ApiResponse<ChannelResponse> accept(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long channelId) {
        return ApiResponse.success(useCase.acceptInvitation(accountId, roomId, channelId));
    }

    @GetMapping("/channels/{channelId}/members")
    @Operation(summary = "채널 참여자 목록")
    public ApiResponse<List<ChannelMemberResponse>> members(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long channelId) {
        return ApiResponse.success(useCase.getMembers(accountId, roomId, channelId));
    }

    @PostMapping("/channels/{channelId}/members")
    @Operation(summary = "채널 멤버 추가")
    public ApiResponse<List<ChannelMemberResponse>> addMembers(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long channelId, @Valid @RequestBody ChannelMemberAddRequest request) {
        return ApiResponse.success(useCase.addMembers(accountId, roomId, channelId, request.toCommand()));
    }

    @DeleteMapping("/channels/{channelId}/members/{targetId}")
    @Operation(summary = "채널 멤버 내보내기")
    public ApiResponse<Void> kick(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long channelId, @PathVariable Long targetId) {
        useCase.kickMember(accountId, roomId, channelId, targetId);
        return ApiResponse.empty();
    }

    @PostMapping("/direct-channels")
    @Operation(summary = "1:1 대화 시작")
    public ApiResponse<ChannelResponse> direct(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody DirectChannelRequest request) {
        return ApiResponse.success(useCase.openDirect(accountId, roomId, request.toCommand()));
    }

    @PostMapping("/channels/{channelId}/hide")
    @Operation(summary = "1:1 대화 숨기기")
    public ApiResponse<Void> hide(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long channelId) {
        useCase.hideDirect(accountId, roomId, channelId);
        return ApiResponse.empty();
    }
}
