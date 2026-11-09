package com.fiveoutoffive.moa_backend.task.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.task.application.result.SprintResponse;
import com.fiveoutoffive.moa_backend.task.application.usecase.TaskUseCase;
import com.fiveoutoffive.moa_backend.task.presentation.api.request.SprintStartRequest;
import com.fiveoutoffive.moa_backend.task.presentation.api.request.SprintUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/sprints")
@RequiredArgsConstructor
@Tag(name = "09. Task", description = "업무")
public class SprintController {

    private final TaskUseCase useCase;

    @PostMapping
    @Operation(summary = "스프린트 시작")
    public ApiResponse<SprintResponse> start(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody SprintStartRequest request) {
        return ApiResponse.success(useCase.startSprint(accountId, roomId, request.toCommand()));
    }

    @GetMapping("/current")
    @Operation(summary = "진행 중 스프린트 조회")
    public ApiResponse<SprintResponse> current(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.getCurrentSprint(accountId, roomId));
    }

    @GetMapping
    @Operation(summary = "지난 스프린트 목록")
    public ApiResponse<PageResponse<SprintResponse>> list(@LoginAccount Long accountId, @PathVariable Long roomId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(useCase.getClosedSprints(accountId, roomId, Pages.of(page, size, Sort.by(Sort.Direction.DESC, "closedAt"))));
    }

    @PatchMapping("/{sprintId:\\d+}")
    @Operation(summary = "스프린트 수정")
    public ApiResponse<SprintResponse> update(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long sprintId, @Valid @RequestBody SprintUpdateRequest request) {
        return ApiResponse.success(useCase.updateSprint(accountId, roomId, sprintId, request.toCommand()));
    }

    @PostMapping("/{sprintId:\\d+}/close")
    @Operation(summary = "스프린트 마감")
    public ApiResponse<SprintResponse> close(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long sprintId) {
        return ApiResponse.success(useCase.closeSprint(accountId, roomId, sprintId));
    }
}
