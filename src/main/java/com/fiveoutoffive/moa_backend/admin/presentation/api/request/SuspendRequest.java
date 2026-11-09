package com.fiveoutoffive.moa_backend.admin.presentation.api.request;

import com.fiveoutoffive.moa_backend.account.domain.model.SuspensionPeriod;
import com.fiveoutoffive.moa_backend.admin.application.command.SuspendCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SuspendRequest(
        @NotNull SuspensionPeriod period,
        @NotBlank @Size(max=500) String reason,
        Long reportId
) {

    public SuspendCommand toCommand() {
        return new SuspendCommand(period, reason, reportId);
    }
}
