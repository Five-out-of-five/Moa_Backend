package com.fiveoutoffive.moa_backend.dashboard.presentation.api.request;

import com.fiveoutoffive.moa_backend.dashboard.domain.model.WidgetType;
import jakarta.validation.constraints.NotNull;

public record WidgetPlacementRequest(Long widgetId, WidgetType widgetType, @NotNull Integer x, @NotNull Integer y, @NotNull Integer w, @NotNull Integer h) {
}
