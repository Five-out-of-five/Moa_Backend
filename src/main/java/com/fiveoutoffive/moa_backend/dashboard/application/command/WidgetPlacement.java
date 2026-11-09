package com.fiveoutoffive.moa_backend.dashboard.application.command;

import com.fiveoutoffive.moa_backend.dashboard.domain.model.WidgetType;

public record WidgetPlacement(Long widgetId, WidgetType widgetType, int x, int y, int w, int h) {
}
