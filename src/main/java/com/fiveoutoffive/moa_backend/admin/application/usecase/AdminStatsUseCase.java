package com.fiveoutoffive.moa_backend.admin.application.usecase;

import com.fiveoutoffive.moa_backend.admin.application.result.AiUsageStatsResponse;
import com.fiveoutoffive.moa_backend.admin.application.result.AuditLogResponse;
import com.fiveoutoffive.moa_backend.admin.application.result.StatsOverviewResponse;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;

public interface AdminStatsUseCase {

    StatsOverviewResponse getOverview(String unit, LocalDate from, LocalDate to);

    AiUsageStatsResponse getAiUsage(String unit, LocalDate from, LocalDate to, AiFeature feature);

    PageResponse<AuditLogResponse> getAuditLogs(Long adminId, String actionType, String targetType, LocalDate from, LocalDate to, Pageable pageable);
}
