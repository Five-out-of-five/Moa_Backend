package com.fiveoutoffive.moa_backend.chat.presentation.api.request;

import com.fiveoutoffive.moa_backend.chat.application.command.DirectChannelCommand;
import jakarta.validation.constraints.NotNull;

public record DirectChannelRequest(
        @NotNull Long targetAccountId
) {

    public DirectChannelCommand toCommand() {
        return new DirectChannelCommand(targetAccountId);
    }
}
