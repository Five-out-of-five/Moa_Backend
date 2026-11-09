package com.fiveoutoffive.moa_backend.task.presentation.api.request;

import com.fiveoutoffive.moa_backend.task.application.command.TaskCreateCommand;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record TaskCreateRequest(
        @NotBlank @Size(max=100) String title,
        @Size(max=1000) String description,
        @NotNull Long assigneeAccountId,
        LocalDate dueDate,
        TaskPriority priority,
        Long sourceCardId,
        Long sourceVoteId,
        @Size(max=5) List<Long> fileIds
) {

    public TaskCreateCommand toCommand() {
        return new TaskCreateCommand(title, description, assigneeAccountId, dueDate, priority, sourceCardId, sourceVoteId, fileIds);
    }
}
