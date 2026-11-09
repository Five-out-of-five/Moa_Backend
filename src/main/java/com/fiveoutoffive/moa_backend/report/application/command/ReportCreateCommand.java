package com.fiveoutoffive.moa_backend.report.application.command;

import com.fiveoutoffive.moa_backend.report.domain.model.ReportReason;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportTargetType;

public record ReportCreateCommand(
        ReportTargetType targetType,
        Long targetId,
        ReportReason reason,
        String detail
) {
}
