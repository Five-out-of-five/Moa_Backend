package com.fiveoutoffive.moa_backend.task.presentation.api.request;

import com.fiveoutoffive.moa_backend.task.application.command.TaskMoveCommand;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TaskMoveRequest(
        @NotNull TaskStatus status,
        @NotNull @Min(0) Integer toIndex,
        @NotNull Long version
) {

    public TaskMoveCommand toCommand() {
        return new TaskMoveCommand(status, toIndex, version);
    }
}
