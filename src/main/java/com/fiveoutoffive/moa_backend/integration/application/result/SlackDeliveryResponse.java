package com.fiveoutoffive.moa_backend.integration.application.result;

import com.fiveoutoffive.moa_backend.integration.domain.model.SlackDeliveryStatus;
import com.fiveoutoffive.moa_backend.integration.domain.model.SlackEventType;
import java.time.LocalDateTime;

public record SlackDeliveryResponse(SlackEventType eventType, SlackDeliveryStatus status, Integer retryCount, String errorMessage, LocalDateTime createdAt) {
}
