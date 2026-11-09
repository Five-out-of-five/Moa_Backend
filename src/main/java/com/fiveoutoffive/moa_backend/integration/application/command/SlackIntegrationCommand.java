package com.fiveoutoffive.moa_backend.integration.application.command;

import com.fiveoutoffive.moa_backend.integration.domain.model.SlackEventType;
import java.util.List;

public record SlackIntegrationCommand(
        String webhookUrl,
        List<SlackEventType> eventTypes
) {
}
