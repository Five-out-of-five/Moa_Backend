package com.fiveoutoffive.moa_backend.vote.application.command;

import java.time.OffsetDateTime;

public record RevoteCommand(
        OffsetDateTime deadlineAt
) {
}
