package com.fiveoutoffive.moa_backend.admin.presentation.api;

import com.fiveoutoffive.moa_backend.admin.application.result.AdminReportDetailResponse;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminReportResponse;
import com.fiveoutoffive.moa_backend.admin.application.usecase.AdminReportUseCase;
import com.fiveoutoffive.moa_backend.admin.presentation.api.request.ReportRejectRequest;
import com.fiveoutoffive.moa_backend.admin.presentation.api.request.ReportResolveRequest;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportReason;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportStatus;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportTargetType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@Tag(name = "19. Admin", description = "서비스 관리자")
public class AdminReportController {

    private final AdminReportUseCase useCase;

    @GetMapping
    @Operation(summary = "[관리자] 신고 목록")
    public ApiResponse<PageResponse<AdminReportResponse>> list(@RequestParam(required = false) ReportStatus status, @RequestParam(required = false) ReportTargetType targetType, @RequestParam(required = false) ReportReason reason, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(useCase.getReports(status, targetType, reason, Pages.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"))));
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "[관리자] 신고 상세")
    public ApiResponse<AdminReportDetailResponse> detail(@PathVariable Long reportId) {
        return ApiResponse.success(useCase.getReport(reportId));
    }

    @PostMapping("/{reportId}/review")
    @Operation(summary = "[관리자] 신고 처리 시작")
    public ApiResponse<AdminReportDetailResponse> review(@LoginAccount Long adminId, @PathVariable Long reportId) {
        return ApiResponse.success(useCase.startReview(adminId, reportId));
    }

    @PostMapping("/{reportId}/resolve")
    @Operation(summary = "[관리자] 신고 조치 완료")
    public ApiResponse<AdminReportDetailResponse> resolve(@LoginAccount Long adminId, @PathVariable Long reportId, @Valid @RequestBody ReportResolveRequest request) {
        return ApiResponse.success(useCase.resolve(adminId, reportId, request.toCommand()));
    }

    @PostMapping("/{reportId}/reject")
    @Operation(summary = "[관리자] 신고 반려")
    public ApiResponse<AdminReportDetailResponse> reject(@LoginAccount Long adminId, @PathVariable Long reportId, @Valid @RequestBody ReportRejectRequest request) {
        return ApiResponse.success(useCase.reject(adminId, reportId, request.toCommand()));
    }
}
