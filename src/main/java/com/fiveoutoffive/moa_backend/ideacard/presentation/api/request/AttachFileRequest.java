package com.fiveoutoffive.moa_backend.ideacard.presentation.api.request;

import com.fiveoutoffive.moa_backend.ideacard.application.command.AttachFileCommand;
import jakarta.validation.constraints.NotNull;

public record AttachFileRequest(
        @NotNull Long fileId
) {

    public AttachFileCommand toCommand() {
        return new AttachFileCommand(fileId);
    }
}
