package com.fiveoutoffive.moa_backend.chat.presentation.api;

import com.fiveoutoffive.moa_backend.chat.application.result.MessageResponse;
import com.fiveoutoffive.moa_backend.chat.application.usecase.ChatUseCase;
import com.fiveoutoffive.moa_backend.chat.presentation.api.request.MessageSendRequest;
import com.fiveoutoffive.moa_backend.chat.presentation.api.request.MessageUpdateRequest;
import com.fiveoutoffive.moa_backend.chat.presentation.api.request.ReadRequest;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.CursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/channels/{channelId}")
@RequiredArgsConstructor
@Tag(name = "13. Chat", description = "채팅")
public class MessageController {

    private final ChatUseCase useCase;

    @GetMapping("/messages")
    @Operation(summary = "메시지 목록 (커서)")
    public ApiResponse<CursorResponse<MessageResponse>> list(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long channelId, @RequestParam(required = false) Long before, @RequestParam(required = false) Long after, @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.success(useCase.getMessages(accountId, roomId, channelId, before, after, size));
    }

    @PostMapping("/messages")
    @Operation(summary = "메시지 전송")
    public ApiResponse<MessageResponse> send(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long channelId, @Valid @RequestBody MessageSendRequest request) {
        return ApiResponse.success(useCase.send(accountId, roomId, channelId, request.toCommand()));
    }

    @PatchMapping("/messages/{messageId}")
    @Operation(summary = "메시지 수정")
    public ApiResponse<MessageResponse> update(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long channelId, @PathVariable Long messageId, @Valid @RequestBody MessageUpdateRequest request) {
        return ApiResponse.success(useCase.updateMessage(accountId, roomId, channelId, messageId, request.toCommand()));
    }

    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "메시지 삭제")
    public ApiResponse<Void> delete(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long channelId, @PathVariable Long messageId) {
        useCase.deleteMessage(accountId, roomId, channelId, messageId);
        return ApiResponse.empty();
    }

    @PutMapping("/read")
    @Operation(summary = "읽음 처리")
    public ApiResponse<Void> read(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long channelId, @Valid @RequestBody ReadRequest request) {
        useCase.markRead(accountId, roomId, channelId, request.toCommand());
        return ApiResponse.empty();
    }
}
