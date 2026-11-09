package com.fiveoutoffive.moa_backend.admin.application.command;

public record ReportRejectCommand(
        String actionReason,
        Boolean falseReport
) {
}
