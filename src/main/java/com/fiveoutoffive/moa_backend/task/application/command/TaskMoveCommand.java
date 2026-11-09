package com.fiveoutoffive.moa_backend.task.application.command;

import com.fiveoutoffive.moa_backend.task.domain.model.TaskStatus;

public record TaskMoveCommand(
        TaskStatus status,
        Integer toIndex,
        Long version
) {
}
