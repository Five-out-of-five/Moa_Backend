package com.fiveoutoffive.moa_backend.integration.presentation.api.request;

import com.fiveoutoffive.moa_backend.integration.application.command.NotionExportCommand;
import jakarta.validation.constraints.NotNull;

public record NotionExportRequest(
        @NotNull Long sessionId
) {

    public NotionExportCommand toCommand() {
        return new NotionExportCommand(sessionId);
    }
}
