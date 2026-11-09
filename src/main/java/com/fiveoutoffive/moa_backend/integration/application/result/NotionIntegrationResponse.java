package com.fiveoutoffive.moa_backend.integration.application.result;

import com.fiveoutoffive.moa_backend.integration.domain.model.NotionExportMode;

public record NotionIntegrationResponse(boolean connected, String workspaceName, TargetDatabase targetDatabase, NotionExportMode exportMode, Boolean enabled, boolean tokenExpired) {

    public record TargetDatabase(String id, String title) {
    }
}
