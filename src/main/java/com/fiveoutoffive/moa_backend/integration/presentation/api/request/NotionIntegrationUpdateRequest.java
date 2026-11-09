package com.fiveoutoffive.moa_backend.integration.presentation.api.request;

import com.fiveoutoffive.moa_backend.integration.application.command.NotionIntegrationUpdateCommand;
import com.fiveoutoffive.moa_backend.integration.domain.model.NotionExportMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotionIntegrationUpdateRequest(
        @NotBlank String targetDatabaseId,
        @NotNull NotionExportMode exportMode
) {

    public NotionIntegrationUpdateCommand toCommand() {
        return new NotionIntegrationUpdateCommand(targetDatabaseId, exportMode);
    }
}
