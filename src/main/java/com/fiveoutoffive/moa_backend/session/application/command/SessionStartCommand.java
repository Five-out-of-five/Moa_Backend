package com.fiveoutoffive.moa_backend.session.application.command;

import java.time.OffsetDateTime;

public record SessionStartCommand(
        String title,
        OffsetDateTime deadlineAt
) {
}
