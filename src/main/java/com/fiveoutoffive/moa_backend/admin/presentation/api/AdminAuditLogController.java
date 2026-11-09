package com.fiveoutoffive.moa_backend.admin.presentation.api;

import com.fiveoutoffive.moa_backend.admin.application.result.AuditLogResponse;
import com.fiveoutoffive.moa_backend.admin.application.usecase.AdminStatsUseCase;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "19. Admin", description = "서비스 관리자")
public class AdminAuditLogController {

    private final AdminStatsUseCase useCase;

    @GetMapping("/api/v1/admin/audit-logs")
    @Operation(summary = "[관리자] 감사 로그")
    public ApiResponse<PageResponse<AuditLogResponse>> list(@RequestParam(required = false) Long adminId, @RequestParam(required = false) String actionType, @RequestParam(required = false) String targetType, @RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to,
                                                            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(useCase.getAuditLogs(adminId, actionType, targetType, from, to, Pages.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }
}
