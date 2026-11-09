package com.fiveoutoffive.moa_backend.mood.application.command;

import com.fiveoutoffive.moa_backend.mood.domain.model.Mood;

public record MoodUpsertCommand(
        Mood mood,
        String comment
) {
}
