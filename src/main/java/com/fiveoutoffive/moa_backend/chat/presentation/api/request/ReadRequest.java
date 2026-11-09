package com.fiveoutoffive.moa_backend.chat.presentation.api.request;

import com.fiveoutoffive.moa_backend.chat.application.command.ReadCommand;
import jakarta.validation.constraints.NotNull;

public record ReadRequest(
        @NotNull Long lastReadMessageId
) {

    public ReadCommand toCommand() {
        return new ReadCommand(lastReadMessageId);
    }
}
