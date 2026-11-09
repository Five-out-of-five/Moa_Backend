package com.fiveoutoffive.moa_backend.ai.application.service;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 방별 일일 AI 호출 한도 (application.yaml app.ai.quota). */
@Component
public class AiQuotaProperties {

    private final Map<AiFeature, Integer> limits;

    public AiQuotaProperties(@Value("${app.ai.quota.assistant}") int assistant, @Value("${app.ai.quota.summary}") int summary, @Value("${app.ai.quota.compromise}") int compromise,
                             @Value("${app.ai.quota.layout}") int layout, @Value("${app.ai.quota.tone}") int tone, @Value("${app.ai.quota.conflict}") int conflict) {
        this.limits = Map.of(AiFeature.ASSISTANT, assistant, AiFeature.SUMMARY, summary, AiFeature.COMPROMISE, compromise, AiFeature.LAYOUT, layout, AiFeature.TONE, tone, AiFeature.CONFLICT, conflict, AiFeature.TASK_RECOMMEND, 0);
    }

    public int limitOf(AiFeature feature) {
        return limits.get(feature);
    }
}
