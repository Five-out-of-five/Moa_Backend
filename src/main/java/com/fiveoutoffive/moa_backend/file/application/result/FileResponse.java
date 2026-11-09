package com.fiveoutoffive.moa_backend.file.application.result;

import com.fiveoutoffive.moa_backend.file.domain.model.FileGroup;

public record FileResponse(Long fileId, String originalName, String mimeType, Long sizeBytes, FileGroup purpose, String previewUrl) {
}
