package com.fiveoutoffive.moa_backend.file.application.result;

import java.time.LocalDateTime;

public record DownloadUrlResponse(String url, LocalDateTime expiresAt) {
}
