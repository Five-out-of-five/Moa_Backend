package com.fiveoutoffive.moa_backend.dashboard.application.result;

import com.fiveoutoffive.moa_backend.dashboard.application.command.WidgetPlacement;
import java.util.List;

public record LayoutRecommendResponse(List<WidgetPlacement> widgets, String reason) {
}
