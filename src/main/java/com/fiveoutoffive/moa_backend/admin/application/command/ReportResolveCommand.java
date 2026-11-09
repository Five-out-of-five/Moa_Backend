package com.fiveoutoffive.moa_backend.admin.application.command;

import com.fiveoutoffive.moa_backend.account.domain.model.SuspensionPeriod;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportAction;

public record ReportResolveCommand(
        ReportAction action,
        String actionReason,
        SuspensionPeriod suspensionPeriod,
        Boolean falseReport
) {
}
