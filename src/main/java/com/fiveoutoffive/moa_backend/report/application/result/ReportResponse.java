package com.fiveoutoffive.moa_backend.report.application.result;

import com.fiveoutoffive.moa_backend.report.domain.model.ReportStatus;
import java.time.LocalDateTime;

public record ReportResponse(Long reportId, ReportStatus status, LocalDateTime createdAt) {
}
