package com.fiveoutoffive.moa_backend.task.presentation.api;

import com.fiveoutoffive.moa_backend.file.application.result.FileResponse;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.task.application.result.TaskDetailResponse;
import com.fiveoutoffive.moa_backend.task.application.result.TaskSummaryResponse;
import com.fiveoutoffive.moa_backend.task.application.usecase.TaskUseCase;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskPriority;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskStatus;
import com.fiveoutoffive.moa_backend.task.presentation.api.request.AssigneeChangeRequest;
import com.fiveoutoffive.moa_backend.task.presentation.api.request.TaskAttachFileRequest;
import com.fiveoutoffive.moa_backend.task.presentation.api.request.TaskCreateRequest;
import com.fiveoutoffive.moa_backend.task.presentation.api.request.TaskMoveRequest;
import com.fiveoutoffive.moa_backend.task.presentation.api.request.TaskSprintChangeRequest;
import com.fiveoutoffive.moa_backend.task.presentation.api.request.TaskUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/tasks")
@RequiredArgsConstructor
@Tag(name = "09. Task", description = "업무")
public class TaskController {

    private final TaskUseCase useCase;

    @GetMapping
    @Operation(summary = "업무 보드 목록")
    public ApiResponse<List<TaskSummaryResponse>> list(@LoginAccount Long accountId, @PathVariable Long roomId, @RequestParam(required = false) TaskStatus status, @RequestParam(required = false) Long assigneeId,
                                                       @RequestParam(required = false) TaskPriority priority, @RequestParam(required = false) Long sprintId, @RequestParam(defaultValue = "false") boolean backlog) {
        return ApiResponse.success(useCase.getTasks(accountId, roomId, status, assigneeId, priority, sprintId, backlog));
    }

    @PostMapping
    @Operation(summary = "업무 등록 (카드·투표에서 전환 포함)")
    public ApiResponse<TaskDetailResponse> create(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody TaskCreateRequest request) {
        return ApiResponse.success(useCase.create(accountId, roomId, request.toCommand()));
    }

    @GetMapping("/{taskId:\\d+}")
    @Operation(summary = "업무 상세")
    public ApiResponse<TaskDetailResponse> detail(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long taskId) {
        return ApiResponse.success(useCase.getDetail(accountId, roomId, taskId));
    }

    @PatchMapping("/{taskId:\\d+}")
    @Operation(summary = "업무 내용 수정")
    public ApiResponse<TaskDetailResponse> update(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long taskId, @Valid @RequestBody TaskUpdateRequest request) {
        return ApiResponse.success(useCase.update(accountId, roomId, taskId, request.toCommand()));
    }

    @PatchMapping("/{taskId:\\d+}/status")
    @Operation(summary = "업무 상태 변경·칸반 이동")
    public ApiResponse<List<TaskSummaryResponse>> move(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long taskId, @Valid @RequestBody TaskMoveRequest request) {
        return ApiResponse.success(useCase.move(accountId, roomId, taskId, request.toCommand()));
    }

    @PatchMapping("/{taskId:\\d+}/sprint")
    @Operation(summary = "업무 스프린트 이동")
    public ApiResponse<TaskDetailResponse> changeSprint(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long taskId, @RequestBody TaskSprintChangeRequest request) {
        return ApiResponse.success(useCase.changeSprint(accountId, roomId, taskId, request.toCommand()));
    }

    @PatchMapping("/{taskId:\\d+}/assignee")
    @Operation(summary = "담당자 변경")
    public ApiResponse<TaskDetailResponse> changeAssignee(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long taskId, @Valid @RequestBody AssigneeChangeRequest request) {
        return ApiResponse.success(useCase.changeAssignee(accountId, roomId, taskId, request.toCommand()));
    }

    @PostMapping("/{taskId:\\d+}/reject")
    @Operation(summary = "업무 반려")
    public ApiResponse<TaskDetailResponse> reject(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long taskId) {
        return ApiResponse.success(useCase.reject(accountId, roomId, taskId));
    }

    @DeleteMapping("/{taskId:\\d+}")
    @Operation(summary = "업무 삭제")
    public ApiResponse<Void> delete(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long taskId) {
        useCase.delete(accountId, roomId, taskId);
        return ApiResponse.empty();
    }

    @PostMapping("/{taskId:\\d+}/files")
    @Operation(summary = "업무 첨부 추가")
    public ApiResponse<List<FileResponse>> attach(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long taskId, @Valid @RequestBody TaskAttachFileRequest request) {
        return ApiResponse.success(useCase.attachFile(accountId, roomId, taskId, request.toCommand()));
    }

    @DeleteMapping("/{taskId:\\d+}/files/{fileId}")
    @Operation(summary = "업무 첨부 삭제")
    public ApiResponse<Void> removeFile(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long taskId, @PathVariable Long fileId) {
        useCase.removeFile(accountId, roomId, taskId, fileId);
        return ApiResponse.empty();
    }
}
