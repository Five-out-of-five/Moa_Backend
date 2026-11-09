package com.fiveoutoffive.moa_backend.task.application.command;

import com.fiveoutoffive.moa_backend.task.domain.model.TaskPriority;
import java.time.LocalDate;

public record TaskUpdateCommand(
        String title,
        String description,
        LocalDate dueDate,
        TaskPriority priority
) {
}
