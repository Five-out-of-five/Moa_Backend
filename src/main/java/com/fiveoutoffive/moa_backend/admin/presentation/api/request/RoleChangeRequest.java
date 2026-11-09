package com.fiveoutoffive.moa_backend.admin.presentation.api.request;

import com.fiveoutoffive.moa_backend.account.domain.model.AccountRole;
import com.fiveoutoffive.moa_backend.admin.application.command.RoleChangeCommand;
import jakarta.validation.constraints.NotNull;

public record RoleChangeRequest(
        @NotNull AccountRole role
) {

    public RoleChangeCommand toCommand() {
        return new RoleChangeCommand(role);
    }
}
