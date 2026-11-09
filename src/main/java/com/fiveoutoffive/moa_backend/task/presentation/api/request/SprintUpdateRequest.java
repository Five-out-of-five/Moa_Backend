package com.fiveoutoffive.moa_backend.task.presentation.api.request;

import com.fiveoutoffive.moa_backend.task.application.command.SprintUpdateCommand;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SprintUpdateRequest(
        @Size(min=1,max=50) String name,
        LocalDate endDate
) {

    public SprintUpdateCommand toCommand() {
        return new SprintUpdateCommand(name, endDate);
    }
}
