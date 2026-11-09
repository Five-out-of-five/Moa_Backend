package com.fiveoutoffive.moa_backend.room.presentation.api.request;

import com.fiveoutoffive.moa_backend.room.application.command.RoomDeleteCommand;
import jakarta.validation.constraints.NotBlank;

public record RoomDeleteRequest(
        @NotBlank String confirmName
) {

    public RoomDeleteCommand toCommand() {
        return new RoomDeleteCommand(confirmName);
    }
}
