package com.fiveoutoffive.moa_backend.ai.presentation.api.request;

import com.fiveoutoffive.moa_backend.ai.application.command.SummaryUpdateCommand;
import jakarta.validation.constraints.NotBlank;

public record SummaryUpdateRequest(
        @NotBlank String content
) {

    public SummaryUpdateCommand toCommand() {
        return new SummaryUpdateCommand(content);
    }
}
