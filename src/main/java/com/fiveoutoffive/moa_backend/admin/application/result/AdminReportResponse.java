package com.fiveoutoffive.moa_backend.admin.application.result;

import com.fiveoutoffive.moa_backend.report.domain.model.ReportReason;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportStatus;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportTargetType;
import java.time.LocalDateTime;

public record AdminReportResponse(Long reportId, ReportTargetType targetType, ReportReason reason, ReportStatus status, Person reporter, Person targetAccount, RoomRef room, LocalDateTime createdAt, boolean overdue) {

    public record Person(Long accountId, String name) {
    }

    public record RoomRef(Long roomId, String name) {
    }
}
