package com.fiveoutoffive.moa_backend.admin.application.result;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AiUsageStatsResponse(List<ByFeature> byFeature, List<Point> series, Threshold threshold) {

    public record ByFeature(AiFeature feature, long calls, long failedCalls, long promptTokens, long outputTokens, BigDecimal costAmount) {
    }

    public record Point(LocalDate periodStart, AiFeature feature, long calls, BigDecimal costAmount) {
    }

    public record Threshold(BigDecimal costAmount, boolean exceeded) {
    }
}
