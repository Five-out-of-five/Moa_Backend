package com.fiveoutoffive.moa_backend.room.application.command;

public record RoomCreateCommand(
        String name,
        String description
) {
}
