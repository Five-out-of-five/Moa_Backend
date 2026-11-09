package com.fiveoutoffive.moa_backend.integration.presentation.api.request;

import com.fiveoutoffive.moa_backend.integration.application.command.SlackIntegrationCommand;
import com.fiveoutoffive.moa_backend.integration.domain.model.SlackEventType;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SlackIntegrationRequest(
        String webhookUrl,
        @NotNull List<SlackEventType> eventTypes
) {

    public SlackIntegrationCommand toCommand() {
        return new SlackIntegrationCommand(webhookUrl, eventTypes);
    }
}
