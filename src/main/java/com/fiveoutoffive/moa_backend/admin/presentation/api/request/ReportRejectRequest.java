package com.fiveoutoffive.moa_backend.admin.presentation.api.request;

import com.fiveoutoffive.moa_backend.admin.application.command.ReportRejectCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReportRejectRequest(
        @NotBlank @Size(max=500) String actionReason,
        Boolean falseReport
) {

    public ReportRejectCommand toCommand() {
        return new ReportRejectCommand(actionReason, falseReport);
    }
}
