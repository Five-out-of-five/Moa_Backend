package com.fiveoutoffive.moa_backend.dashboard.presentation.api;

import com.fiveoutoffive.moa_backend.dashboard.application.command.WidgetPlacement;
import com.fiveoutoffive.moa_backend.dashboard.application.result.DashboardResponse;
import com.fiveoutoffive.moa_backend.dashboard.application.result.LayoutRecommendResponse;
import com.fiveoutoffive.moa_backend.dashboard.application.usecase.DashboardUseCase;
import com.fiveoutoffive.moa_backend.dashboard.presentation.api.request.LayoutRecommendRequest;
import com.fiveoutoffive.moa_backend.dashboard.presentation.api.request.LayoutUpdateRequest;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/dashboard/ai-recommendations")
@RequiredArgsConstructor
@Tag(name = "05. Dashboard", description = "대시보드")
public class DashboardAiController {

    private final DashboardUseCase useCase;

    @PostMapping
    @Operation(summary = "AI 대시보드 구성 추천")
    public ApiResponse<LayoutRecommendResponse> recommend(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody LayoutRecommendRequest request) {
        return ApiResponse.success(useCase.recommend(accountId, roomId, request.purpose()));
    }

    @PostMapping("/apply")
    @Operation(summary = "AI 구성 적용")
    public ApiResponse<DashboardResponse> apply(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody LayoutUpdateRequest request) {
        return ApiResponse.success(useCase.applyRecommendation(accountId, roomId,
                request.widgets().stream().map(r -> new WidgetPlacement(null, r.widgetType(), r.x(), r.y(), r.w(), r.h())).toList()));
    }

    @PostMapping("/undo")
    @Operation(summary = "AI 구성 되돌리기")
    public ApiResponse<DashboardResponse> undo(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.undoRecommendation(accountId, roomId));
    }
}
