package com.fiveoutoffive.moa_backend.report.presentation.api.request;

import com.fiveoutoffive.moa_backend.report.application.command.ReportCreateCommand;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportReason;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportTargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportCreateRequest(
        @NotNull ReportTargetType targetType,
        @NotNull Long targetId,
        @NotNull ReportReason reason,
        @Size(max=500) String detail
) {

    public ReportCreateCommand toCommand() {
        return new ReportCreateCommand(targetType, targetId, reason, detail);
    }
}
