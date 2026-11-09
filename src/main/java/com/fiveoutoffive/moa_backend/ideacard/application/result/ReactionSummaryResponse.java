package com.fiveoutoffive.moa_backend.ideacard.application.result;

public record ReactionSummaryResponse(String emoji, long count, boolean reactedByMe) {
}
