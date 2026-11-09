package com.fiveoutoffive.moa_backend.ai.application.result;

import java.util.List;

public record AiEmbeddingPayload(List<Double> embedding, String model) {
}
