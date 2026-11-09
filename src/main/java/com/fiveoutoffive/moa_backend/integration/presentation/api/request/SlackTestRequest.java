package com.fiveoutoffive.moa_backend.integration.presentation.api.request;

import com.fiveoutoffive.moa_backend.integration.application.command.SlackTestCommand;
import jakarta.validation.constraints.NotBlank;

public record SlackTestRequest(
        @NotBlank String webhookUrl
) {

    public SlackTestCommand toCommand() {
        return new SlackTestCommand(webhookUrl);
    }
}
