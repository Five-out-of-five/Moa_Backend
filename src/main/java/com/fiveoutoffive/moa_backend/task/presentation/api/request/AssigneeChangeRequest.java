package com.fiveoutoffive.moa_backend.task.presentation.api.request;

import com.fiveoutoffive.moa_backend.task.application.command.AssigneeChangeCommand;
import jakarta.validation.constraints.NotNull;

public record AssigneeChangeRequest(
        @NotNull Long assigneeAccountId
) {

    public AssigneeChangeCommand toCommand() {
        return new AssigneeChangeCommand(assigneeAccountId);
    }
}
