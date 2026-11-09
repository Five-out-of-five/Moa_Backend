package com.fiveoutoffive.moa_backend.dashboard.presentation.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record LayoutUpdateRequest(@NotEmpty @Valid List<WidgetPlacementRequest> widgets) {
}
