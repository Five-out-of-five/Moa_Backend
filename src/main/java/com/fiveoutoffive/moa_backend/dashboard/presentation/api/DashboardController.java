package com.fiveoutoffive.moa_backend.dashboard.presentation.api;

import com.fiveoutoffive.moa_backend.dashboard.application.command.WidgetPlacement;
import com.fiveoutoffive.moa_backend.dashboard.application.result.DashboardResponse;
import com.fiveoutoffive.moa_backend.dashboard.application.usecase.DashboardUseCase;
import com.fiveoutoffive.moa_backend.dashboard.presentation.api.request.LayoutUpdateRequest;
import com.fiveoutoffive.moa_backend.dashboard.presentation.api.request.WidgetPlacementRequest;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/dashboard")
@RequiredArgsConstructor
@Tag(name = "05. Dashboard", description = "대시보드")
public class DashboardController {

    private final DashboardUseCase useCase;

    @GetMapping
    @Operation(summary = "대시보드 레이아웃 조회")
    public ApiResponse<DashboardResponse> get(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.get(accountId, roomId));
    }

    @PostMapping("/widgets")
    @Operation(summary = "위젯 추가")
    public ApiResponse<DashboardResponse> add(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody WidgetPlacementRequest r) {
        return ApiResponse.success(useCase.addWidget(accountId, roomId, new WidgetPlacement(null, r.widgetType(), r.x(), r.y(), r.w(), r.h())));
    }

    @DeleteMapping("/widgets/{widgetId}")
    @Operation(summary = "위젯 제거")
    public ApiResponse<DashboardResponse> remove(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long widgetId) {
        return ApiResponse.success(useCase.removeWidget(accountId, roomId, widgetId));
    }

    @PutMapping("/layout")
    @Operation(summary = "위젯 배치·크기 저장")
    public ApiResponse<DashboardResponse> layout(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody LayoutUpdateRequest request) {
        return ApiResponse.success(useCase.updateLayout(accountId, roomId, toPlacements(request)));
    }

    @PostMapping("/reset")
    @Operation(summary = "레이아웃 초기화")
    public ApiResponse<DashboardResponse> reset(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.reset(accountId, roomId));
    }

    private List<WidgetPlacement> toPlacements(LayoutUpdateRequest request) {
        return request.widgets().stream().map(r -> new WidgetPlacement(r.widgetId(), r.widgetType(), r.x(), r.y(), r.w(), r.h())).toList();
    }
}
