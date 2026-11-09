package com.fiveoutoffive.moa_backend.session.application.result;

import java.time.LocalDateTime;

public record SessionSummaryResponse(Long sessionId, Integer sessionNo, String title, long cardCount, long participantCount, LocalDateTime closedAt) {
}
