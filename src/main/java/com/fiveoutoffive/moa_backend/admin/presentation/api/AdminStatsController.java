package com.fiveoutoffive.moa_backend.admin.presentation.api;

import com.fiveoutoffive.moa_backend.admin.application.result.AiUsageStatsResponse;
import com.fiveoutoffive.moa_backend.admin.application.result.StatsOverviewResponse;
import com.fiveoutoffive.moa_backend.admin.application.usecase.AdminStatsUseCase;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
@Tag(name = "19. Admin", description = "서비스 관리자")
public class AdminStatsController {

    private final AdminStatsUseCase useCase;

    @GetMapping("/overview")
    @Operation(summary = "[관리자] 서비스 지표")
    public ApiResponse<StatsOverviewResponse> overview(@RequestParam String unit, @RequestParam LocalDate from, @RequestParam LocalDate to) {
        return ApiResponse.success(useCase.getOverview(unit, from, to));
    }

    @GetMapping("/ai-usage")
    @Operation(summary = "[관리자] AI 사용량")
    public ApiResponse<AiUsageStatsResponse> aiUsage(@RequestParam String unit, @RequestParam LocalDate from, @RequestParam LocalDate to, @RequestParam(required = false) AiFeature feature) {
        return ApiResponse.success(useCase.getAiUsage(unit, from, to, feature));
    }
}
