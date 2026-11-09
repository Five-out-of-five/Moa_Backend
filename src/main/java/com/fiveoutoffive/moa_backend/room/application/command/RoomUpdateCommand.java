package com.fiveoutoffive.moa_backend.room.application.command;

import java.util.Optional;

public record RoomUpdateCommand(
        String name,
        String description,
        Optional<Long> imageFileId
) {
}
