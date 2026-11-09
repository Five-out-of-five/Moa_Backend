package com.fiveoutoffive.moa_backend.ai.application.result;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import java.time.LocalDateTime;
import java.util.List;

public record AiQuotaResponse(List<Feature> features) {

    public record Feature(AiFeature feature, int used, int limit, LocalDateTime resetAt) {
    }
}
