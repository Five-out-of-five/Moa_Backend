package com.fiveoutoffive.moa_backend.mood.presentation.api.request;

import com.fiveoutoffive.moa_backend.mood.application.command.MoodUpsertCommand;
import com.fiveoutoffive.moa_backend.mood.domain.model.Mood;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MoodUpsertRequest(
        @NotNull Mood mood,
        @Size(max=50) String comment
) {

    public MoodUpsertCommand toCommand() {
        return new MoodUpsertCommand(mood, comment);
    }
}
