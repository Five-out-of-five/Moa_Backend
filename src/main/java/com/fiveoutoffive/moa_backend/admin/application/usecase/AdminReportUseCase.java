package com.fiveoutoffive.moa_backend.admin.application.usecase;

import com.fiveoutoffive.moa_backend.admin.application.command.ReportRejectCommand;
import com.fiveoutoffive.moa_backend.admin.application.command.ReportResolveCommand;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminReportDetailResponse;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminReportResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportReason;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportStatus;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportTargetType;
import org.springframework.data.domain.Pageable;

public interface AdminReportUseCase {

    PageResponse<AdminReportResponse> getReports(ReportStatus status, ReportTargetType targetType, ReportReason reason, Pageable pageable);

    AdminReportDetailResponse getReport(Long reportId);

    AdminReportDetailResponse startReview(Long adminId, Long reportId);

    AdminReportDetailResponse resolve(Long adminId, Long reportId, ReportResolveCommand command);

    AdminReportDetailResponse reject(Long adminId, Long reportId, ReportRejectCommand command);
}
