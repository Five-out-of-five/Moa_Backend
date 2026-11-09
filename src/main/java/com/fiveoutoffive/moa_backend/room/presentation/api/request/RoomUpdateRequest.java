package com.fiveoutoffive.moa_backend.room.presentation.api.request;

import com.fiveoutoffive.moa_backend.room.application.command.RoomUpdateCommand;
import jakarta.validation.constraints.Size;
import java.util.Optional;

public record RoomUpdateRequest(
        @Size(min=2,max=30) String name,
        @Size(max=200) String description,
        Optional<Long> imageFileId
) {

    public RoomUpdateCommand toCommand() {
        return new RoomUpdateCommand(name, description, imageFileId);
    }
}
