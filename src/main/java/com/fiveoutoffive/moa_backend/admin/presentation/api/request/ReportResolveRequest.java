package com.fiveoutoffive.moa_backend.admin.presentation.api.request;

import com.fiveoutoffive.moa_backend.account.domain.model.SuspensionPeriod;
import com.fiveoutoffive.moa_backend.admin.application.command.ReportResolveCommand;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportResolveRequest(
        @NotNull ReportAction action,
        @NotBlank @Size(max=500) String actionReason,
        SuspensionPeriod suspensionPeriod,
        Boolean falseReport
) {

    public ReportResolveCommand toCommand() {
        return new ReportResolveCommand(action, actionReason, suspensionPeriod, falseReport);
    }
}
