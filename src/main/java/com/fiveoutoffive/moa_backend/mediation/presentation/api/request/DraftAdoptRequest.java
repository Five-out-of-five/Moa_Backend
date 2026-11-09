package com.fiveoutoffive.moa_backend.mediation.presentation.api.request;

import com.fiveoutoffive.moa_backend.mediation.application.command.DraftAdoptCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DraftAdoptRequest(
        @NotBlank @Size(max=100) String title,
        @Size(max=1000) String content
) {

    public DraftAdoptCommand toCommand() {
        return new DraftAdoptCommand(title, content);
    }
}
