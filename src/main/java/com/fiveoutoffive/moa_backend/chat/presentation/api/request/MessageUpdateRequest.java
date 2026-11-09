package com.fiveoutoffive.moa_backend.chat.presentation.api.request;

import com.fiveoutoffive.moa_backend.chat.application.command.MessageUpdateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageUpdateRequest(
        @NotBlank @Size(max=2000) String content
) {

    public MessageUpdateCommand toCommand() {
        return new MessageUpdateCommand(content);
    }
}
