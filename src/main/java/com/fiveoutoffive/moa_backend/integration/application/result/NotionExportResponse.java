package com.fiveoutoffive.moa_backend.integration.application.result;

import com.fiveoutoffive.moa_backend.integration.domain.model.NotionExportStatus;
import java.time.LocalDateTime;

public record NotionExportResponse(Long sessionId, String notionPageId, NotionExportStatus status, LocalDateTime exportedAt) {
}
