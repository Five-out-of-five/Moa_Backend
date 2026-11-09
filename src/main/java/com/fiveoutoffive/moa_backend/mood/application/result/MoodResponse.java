package com.fiveoutoffive.moa_backend.mood.application.result;

import com.fiveoutoffive.moa_backend.mood.domain.model.Mood;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MoodResponse(Long accountId, Mood mood, String comment, LocalDate recordedDate, LocalDateTime updatedAt) {
}
