package com.fiveoutoffive.moa_backend.task.presentation.api.request;

import com.fiveoutoffive.moa_backend.task.application.command.TaskSprintChangeCommand;

public record TaskSprintChangeRequest(
        Long sprintId
) {

    public TaskSprintChangeCommand toCommand() {
        return new TaskSprintChangeCommand(sprintId);
    }
}
