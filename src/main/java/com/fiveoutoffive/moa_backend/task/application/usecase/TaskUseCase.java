package com.fiveoutoffive.moa_backend.task.application.usecase;

import com.fiveoutoffive.moa_backend.file.application.result.FileResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.task.application.command.AssigneeChangeCommand;
import com.fiveoutoffive.moa_backend.task.application.command.SprintStartCommand;
import com.fiveoutoffive.moa_backend.task.application.command.SprintUpdateCommand;
import com.fiveoutoffive.moa_backend.task.application.command.TaskAttachFileCommand;
import com.fiveoutoffive.moa_backend.task.application.command.TaskCreateCommand;
import com.fiveoutoffive.moa_backend.task.application.command.TaskMoveCommand;
import com.fiveoutoffive.moa_backend.task.application.command.TaskSprintChangeCommand;
import com.fiveoutoffive.moa_backend.task.application.command.TaskUpdateCommand;
import com.fiveoutoffive.moa_backend.task.application.result.ProgressResponse;
import com.fiveoutoffive.moa_backend.task.application.result.SprintResponse;
import com.fiveoutoffive.moa_backend.task.application.result.TaskDetailResponse;
import com.fiveoutoffive.moa_backend.task.application.result.TaskSummaryResponse;
import com.fiveoutoffive.moa_backend.task.application.result.WorkloadResponse;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskPriority;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface TaskUseCase {

    List<TaskSummaryResponse> getTasks(Long accountId, Long roomId, TaskStatus status, Long assigneeId, TaskPriority priority, Long sprintId, boolean backlog);

    TaskDetailResponse create(Long accountId, Long roomId, TaskCreateCommand command);

    TaskDetailResponse getDetail(Long accountId, Long roomId, Long taskId);

    TaskDetailResponse update(Long accountId, Long roomId, Long taskId, TaskUpdateCommand command);

    List<TaskSummaryResponse> move(Long accountId, Long roomId, Long taskId, TaskMoveCommand command);

    TaskDetailResponse changeSprint(Long accountId, Long roomId, Long taskId, TaskSprintChangeCommand command);

    TaskDetailResponse changeAssignee(Long accountId, Long roomId, Long taskId, AssigneeChangeCommand command);

    TaskDetailResponse reject(Long accountId, Long roomId, Long taskId);

    void delete(Long accountId, Long roomId, Long taskId);

    List<FileResponse> attachFile(Long accountId, Long roomId, Long taskId, TaskAttachFileCommand command);

    void removeFile(Long accountId, Long roomId, Long taskId, Long fileId);

    ProgressResponse getProgress(Long accountId, Long roomId);

    WorkloadResponse getWorkload(Long accountId, Long roomId);

    SprintResponse startSprint(Long accountId, Long roomId, SprintStartCommand command);

    SprintResponse getCurrentSprint(Long accountId, Long roomId);

    PageResponse<SprintResponse> getClosedSprints(Long accountId, Long roomId, Pageable pageable);

    SprintResponse updateSprint(Long accountId, Long roomId, Long sprintId, SprintUpdateCommand command);

    SprintResponse closeSprint(Long accountId, Long roomId, Long sprintId);
}
