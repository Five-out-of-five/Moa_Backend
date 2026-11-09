package com.fiveoutoffive.moa_backend.briefing.presentation.api.request;

import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventType;
import jakarta.validation.constraints.NotNull;

public record BriefingDismissRequest(@NotNull BoardEventType eventType) {
}
