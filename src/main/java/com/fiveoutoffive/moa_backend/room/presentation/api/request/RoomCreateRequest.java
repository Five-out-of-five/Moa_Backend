package com.fiveoutoffive.moa_backend.room.presentation.api.request;

import com.fiveoutoffive.moa_backend.room.application.command.RoomCreateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoomCreateRequest(
        @NotBlank @Size(min=2,max=30) String name,
        @Size(max=200) String description
) {

    public RoomCreateCommand toCommand() {
        return new RoomCreateCommand(name, description);
    }
}
