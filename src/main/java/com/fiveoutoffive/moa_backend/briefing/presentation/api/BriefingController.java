package com.fiveoutoffive.moa_backend.briefing.presentation.api;

import com.fiveoutoffive.moa_backend.briefing.application.result.BriefingResponse;
import com.fiveoutoffive.moa_backend.briefing.application.usecase.BriefingUseCase;
import com.fiveoutoffive.moa_backend.briefing.presentation.api.request.BriefingDismissRequest;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/briefing")
@RequiredArgsConstructor
@Tag(name = "11. Briefing", description = "캐치업 브리핑")
public class BriefingController {

    private final BriefingUseCase useCase;

    @GetMapping
    @Operation(summary = "캐치업 브리핑 조회")
    public ApiResponse<BriefingResponse> get(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.get(accountId, roomId));
    }

    @PostMapping("/check")
    @Operation(summary = "브리핑 모두 확인")
    public ApiResponse<BriefingResponse> check(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.checkAll(accountId, roomId));
    }

    @PostMapping("/dismiss")
    @Operation(summary = "브리핑 항목 개별 소거")
    public ApiResponse<BriefingResponse> dismiss(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody BriefingDismissRequest request) {
        return ApiResponse.success(useCase.dismiss(accountId, roomId, request.eventType()));
    }
}
