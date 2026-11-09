package com.fiveoutoffive.moa_backend.ai.presentation.api;

import com.fiveoutoffive.moa_backend.ai.application.result.ToneCheckResponse;
import com.fiveoutoffive.moa_backend.ai.application.usecase.AiUseCase;
import com.fiveoutoffive.moa_backend.ai.presentation.api.request.ToneCheckRequest;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "15. AI", description = "AI")
public class ToneCheckController {

    private final AiUseCase useCase;

    @PostMapping("/api/v1/rooms/{roomId}/ai/tone-checks")
    @Operation(summary = "톤앤매너 점검")
    public ApiResponse<ToneCheckResponse> check(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody ToneCheckRequest request) {
        return ApiResponse.success(useCase.checkTone(accountId, roomId, request.toCommand()));
    }
}
