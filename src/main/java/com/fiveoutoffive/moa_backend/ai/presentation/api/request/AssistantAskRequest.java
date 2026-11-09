package com.fiveoutoffive.moa_backend.ai.presentation.api.request;

import com.fiveoutoffive.moa_backend.ai.application.command.AssistantAskCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantAskRequest(
        @NotBlank @Size(max=200) String question
) {

    public AssistantAskCommand toCommand() {
        return new AssistantAskCommand(question);
    }
}
