package com.fiveoutoffive.moa_backend.dashboard.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LayoutRecommendRequest(@NotBlank @Size(max = 200) String purpose) {
}
