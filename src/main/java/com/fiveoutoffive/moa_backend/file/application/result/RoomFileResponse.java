package com.fiveoutoffive.moa_backend.file.application.result;

import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import java.time.LocalDateTime;

public record RoomFileResponse(Long fileId, String originalName, String mimeType, Long sizeBytes, MemberRef uploader, String source, Long sourceId, LocalDateTime uploadedAt) {
}
