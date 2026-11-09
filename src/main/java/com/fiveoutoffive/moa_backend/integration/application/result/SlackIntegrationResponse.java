package com.fiveoutoffive.moa_backend.integration.application.result;

import com.fiveoutoffive.moa_backend.integration.domain.model.SlackEventType;
import java.time.LocalDateTime;
import java.util.List;

public record SlackIntegrationResponse(boolean connected, String webhookUrlMasked, List<SlackEventType> eventTypes, Boolean enabled, Integer consecutiveFailCount, LocalDateTime disabledAt) {
}
