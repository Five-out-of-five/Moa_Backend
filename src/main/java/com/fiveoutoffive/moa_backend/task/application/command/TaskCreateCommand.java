package com.fiveoutoffive.moa_backend.task.application.command;

import com.fiveoutoffive.moa_backend.task.domain.model.TaskPriority;
import java.time.LocalDate;
import java.util.List;

public record TaskCreateCommand(
        String title,
        String description,
        Long assigneeAccountId,
        LocalDate dueDate,
        TaskPriority priority,
        Long sourceCardId,
        Long sourceVoteId,
        List<Long> fileIds
) {
}
