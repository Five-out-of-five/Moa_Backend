package com.fiveoutoffive.moa_backend.ai.application.result;

import com.fiveoutoffive.moa_backend.account.domain.model.ToneMode;

public record ToneCheckResponse(boolean needsChange, String suggestion, ToneMode toneMode) {
}
