package com.fiveoutoffive.moa_backend.chat.presentation.api.request;

import com.fiveoutoffive.moa_backend.chat.application.command.MessageSendCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record MessageSendRequest(
        @Size(max=2000) String content,
        @Size(max=5) List<Long> fileIds,
        @NotBlank String clientMessageId
) {

    public MessageSendCommand toCommand() {
        return new MessageSendCommand(content, fileIds, clientMessageId);
    }
}
