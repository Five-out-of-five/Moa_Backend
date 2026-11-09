package com.fiveoutoffive.moa_backend.report.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.report.application.result.ReportResponse;
import com.fiveoutoffive.moa_backend.report.application.usecase.ReportUseCase;
import com.fiveoutoffive.moa_backend.report.presentation.api.request.ReportCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "17. Report", description = "신고")
public class ReportController {

    private final ReportUseCase useCase;

    @PostMapping("/api/v1/reports")
    @Operation(summary = "신고 접수")
    public ApiResponse<ReportResponse> create(@LoginAccount Long accountId, @Valid @RequestBody ReportCreateRequest request) {
        return ApiResponse.success(useCase.create(accountId, request.toCommand()));
    }
}
