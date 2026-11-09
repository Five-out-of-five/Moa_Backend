package com.fiveoutoffive.moa_backend.task.application.command;

import java.time.LocalDate;

public record SprintStartCommand(
        String name,
        LocalDate startDate,
        LocalDate endDate
) {
}
