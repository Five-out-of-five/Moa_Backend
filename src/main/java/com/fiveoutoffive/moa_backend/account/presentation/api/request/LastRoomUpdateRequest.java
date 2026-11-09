package com.fiveoutoffive.moa_backend.account.presentation.api.request;

import com.fiveoutoffive.moa_backend.account.application.command.LastRoomUpdateCommand;
import jakarta.validation.constraints.NotNull;

public record LastRoomUpdateRequest(
        @NotNull Long roomId
) {

    public LastRoomUpdateCommand toCommand() {
        return new LastRoomUpdateCommand(roomId);
    }
}
