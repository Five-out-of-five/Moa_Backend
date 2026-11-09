package com.fiveoutoffive.moa_backend.task.presentation.api.request;

import com.fiveoutoffive.moa_backend.task.application.command.TaskAttachFileCommand;
import jakarta.validation.constraints.NotNull;

public record TaskAttachFileRequest(
        @NotNull Long fileId
) {

    public TaskAttachFileCommand toCommand() {
        return new TaskAttachFileCommand(fileId);
    }
}
