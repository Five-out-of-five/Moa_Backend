package com.fiveoutoffive.moa_backend.admin.application.result;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportAction;
import java.time.LocalDateTime;

public record AdminReportDetailResponse(@JsonUnwrapped AdminReportResponse report, String detail, String snapshot, boolean targetDeleted, ReportAction action, String actionReason, AdminReportResponse.Person handledBy, LocalDateTime handledAt) {
}
