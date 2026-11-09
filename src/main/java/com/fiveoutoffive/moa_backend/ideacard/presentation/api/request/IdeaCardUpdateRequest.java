package com.fiveoutoffive.moa_backend.ideacard.presentation.api.request;

import com.fiveoutoffive.moa_backend.ideacard.application.command.IdeaCardUpdateCommand;
import jakarta.validation.constraints.Size;

public record IdeaCardUpdateRequest(
        @Size(min=1,max=100) String title,
        @Size(max=1000) String content
) {

    public IdeaCardUpdateCommand toCommand() {
        return new IdeaCardUpdateCommand(title, content);
    }
}
