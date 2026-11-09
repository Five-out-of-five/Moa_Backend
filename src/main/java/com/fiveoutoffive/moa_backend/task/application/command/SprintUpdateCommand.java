package com.fiveoutoffive.moa_backend.task.application.command;

import java.time.LocalDate;

public record SprintUpdateCommand(
        String name,
        LocalDate endDate
) {
}
