package com.fiveoutoffive.moa_backend.task.presentation.api.request;

import com.fiveoutoffive.moa_backend.task.application.command.TaskUpdateCommand;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskPriority;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaskUpdateRequest(
        @Size(min=1,max=100) String title,
        @Size(max=1000) String description,
        LocalDate dueDate,
        TaskPriority priority
) {

    public TaskUpdateCommand toCommand() {
        return new TaskUpdateCommand(title, description, dueDate, priority);
    }
}
