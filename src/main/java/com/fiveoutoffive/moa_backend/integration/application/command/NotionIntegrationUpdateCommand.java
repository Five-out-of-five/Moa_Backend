package com.fiveoutoffive.moa_backend.integration.application.command;

import com.fiveoutoffive.moa_backend.integration.domain.model.NotionExportMode;

public record NotionIntegrationUpdateCommand(
        String targetDatabaseId,
        NotionExportMode exportMode
) {
}
