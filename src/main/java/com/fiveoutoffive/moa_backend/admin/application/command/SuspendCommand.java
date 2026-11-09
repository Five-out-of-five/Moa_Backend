package com.fiveoutoffive.moa_backend.admin.application.command;

import com.fiveoutoffive.moa_backend.account.domain.model.SuspensionPeriod;

public record SuspendCommand(
        SuspensionPeriod period,
        String reason,
        Long reportId
) {
}
