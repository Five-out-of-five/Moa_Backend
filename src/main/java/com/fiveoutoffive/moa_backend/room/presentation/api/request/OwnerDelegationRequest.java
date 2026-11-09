package com.fiveoutoffive.moa_backend.room.presentation.api.request;

import com.fiveoutoffive.moa_backend.room.application.command.OwnerDelegationCommand;
import jakarta.validation.constraints.NotNull;

public record OwnerDelegationRequest(
        @NotNull Long targetAccountId
) {

    public OwnerDelegationCommand toCommand() {
        return new OwnerDelegationCommand(targetAccountId);
    }
}
