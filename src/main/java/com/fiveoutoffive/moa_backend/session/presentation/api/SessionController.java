package com.fiveoutoffive.moa_backend.session.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.session.application.result.SessionResponse;
import com.fiveoutoffive.moa_backend.session.application.result.SessionSummaryResponse;
import com.fiveoutoffive.moa_backend.session.application.usecase.SessionUseCase;
import com.fiveoutoffive.moa_backend.session.presentation.api.request.SessionStartRequest;
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
@RequestMapping("/api/v1/rooms/{roomId}/sessions")
@RequiredArgsConstructor
@Tag(name = "07. Session", description = "회차")
public class SessionController {

    private final SessionUseCase useCase;

    @PostMapping
    @Operation(summary = "회차 시작")
    public ApiResponse<SessionResponse> start(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody SessionStartRequest request) {
        return ApiResponse.success(useCase.start(accountId, roomId, request.toCommand()));
    }

    @GetMapping("/current")
    @Operation(summary = "진행 중 회차 조회")
    public ApiResponse<SessionResponse> current(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.getCurrent(accountId, roomId));
    }

    @PostMapping("/{sessionId}/close")
    @Operation(summary = "회차 수동 마감")
    public ApiResponse<SessionResponse> close(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long sessionId) {
        return ApiResponse.success(useCase.close(accountId, roomId, sessionId));
    }

    @GetMapping
    @Operation(summary = "지난 회차 목록")
    public ApiResponse<PageResponse<SessionSummaryResponse>> list(@LoginAccount Long accountId, @PathVariable Long roomId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(useCase.getClosed(accountId, roomId, Pages.of(page, size, Sort.by(Sort.Direction.DESC, "closedAt"))));
    }

    @GetMapping("/{sessionId:\\d+}")
    @Operation(summary = "회차 상세")
    public ApiResponse<SessionResponse> detail(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long sessionId) {
        return ApiResponse.success(useCase.getDetail(accountId, roomId, sessionId));
    }
}
