package com.fiveoutoffive.moa_backend.task.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.task.application.result.ProgressResponse;
import com.fiveoutoffive.moa_backend.task.application.result.WorkloadResponse;
import com.fiveoutoffive.moa_backend.task.application.usecase.TaskUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/tasks")
@RequiredArgsConstructor
@Tag(name = "09. Task", description = "업무")
public class TaskInsightController {

    private final TaskUseCase useCase;

    @GetMapping("/progress")
    @Operation(summary = "진척도")
    public ApiResponse<ProgressResponse> progress(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.getProgress(accountId, roomId));
    }

    @GetMapping("/workload")
    @Operation(summary = "업무 밸런서")
    public ApiResponse<WorkloadResponse> workload(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.getWorkload(accountId, roomId));
    }
}
