package com.fiveoutoffive.moa_backend.session.presentation.api.request;

import com.fiveoutoffive.moa_backend.session.application.command.SessionStartCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record SessionStartRequest(
        @NotBlank @Size(max=50) String title,
        OffsetDateTime deadlineAt
) {

    public SessionStartCommand toCommand() {
        return new SessionStartCommand(title, deadlineAt);
    }
}
