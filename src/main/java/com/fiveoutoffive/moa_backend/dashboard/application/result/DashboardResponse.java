package com.fiveoutoffive.moa_backend.dashboard.application.result;

import com.fiveoutoffive.moa_backend.dashboard.domain.model.WidgetType;
import java.util.List;

public record DashboardResponse(Long layoutVersion, List<Widget> widgets) {

    public record Widget(Long widgetId, WidgetType widgetType, int x, int y, int w, int h) {
    }
}
