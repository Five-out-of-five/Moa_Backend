package com.fiveoutoffive.moa_backend.vote.presentation.api.request;

import com.fiveoutoffive.moa_backend.vote.application.command.RevoteCommand;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record RevoteRequest(
        @NotNull OffsetDateTime deadlineAt
) {

    public RevoteCommand toCommand() {
        return new RevoteCommand(deadlineAt);
    }
}
