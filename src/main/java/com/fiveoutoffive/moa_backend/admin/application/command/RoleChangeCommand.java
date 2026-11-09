package com.fiveoutoffive.moa_backend.admin.application.command;

import com.fiveoutoffive.moa_backend.account.domain.model.AccountRole;

public record RoleChangeCommand(
        AccountRole role
) {
}
