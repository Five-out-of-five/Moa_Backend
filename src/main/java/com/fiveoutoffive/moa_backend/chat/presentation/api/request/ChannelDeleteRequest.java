package com.fiveoutoffive.moa_backend.chat.presentation.api.request;

import com.fiveoutoffive.moa_backend.chat.application.command.ChannelDeleteCommand;
import jakarta.validation.constraints.NotBlank;

public record ChannelDeleteRequest(
        @NotBlank String confirmName
) {

    public ChannelDeleteCommand toCommand() {
        return new ChannelDeleteCommand(confirmName);
    }
}
