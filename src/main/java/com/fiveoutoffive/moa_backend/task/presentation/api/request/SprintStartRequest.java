package com.fiveoutoffive.moa_backend.task.presentation.api.request;

import com.fiveoutoffive.moa_backend.task.application.command.SprintStartCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SprintStartRequest(
        @NotBlank @Size(max=50) String name,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {

    public SprintStartCommand toCommand() {
        return new SprintStartCommand(name, startDate, endDate);
    }
}
