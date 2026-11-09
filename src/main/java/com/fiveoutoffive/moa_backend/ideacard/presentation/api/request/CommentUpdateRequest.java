package com.fiveoutoffive.moa_backend.ideacard.presentation.api.request;

import com.fiveoutoffive.moa_backend.ideacard.application.command.CommentUpdateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
        @NotBlank @Size(max=500) String content
) {

    public CommentUpdateCommand toCommand() {
        return new CommentUpdateCommand(content);
    }
}
