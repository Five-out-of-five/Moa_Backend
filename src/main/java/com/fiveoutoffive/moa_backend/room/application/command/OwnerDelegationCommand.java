package com.fiveoutoffive.moa_backend.room.application.command;

public record OwnerDelegationCommand(
        Long targetAccountId
) {
}
