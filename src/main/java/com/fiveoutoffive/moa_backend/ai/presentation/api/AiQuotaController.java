package com.fiveoutoffive.moa_backend.ai.presentation.api;

import com.fiveoutoffive.moa_backend.ai.application.result.AiQuotaResponse;
import com.fiveoutoffive.moa_backend.ai.application.usecase.AiUseCase;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "15. AI", description = "AI")
public class AiQuotaController {

    private final AiUseCase useCase;

    @GetMapping("/api/v1/rooms/{roomId}/ai/quota")
    @Operation(summary = "AI 남은 호출 횟수")
    public ApiResponse<AiQuotaResponse> quota(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.getQuota(accountId, roomId));
    }
}
