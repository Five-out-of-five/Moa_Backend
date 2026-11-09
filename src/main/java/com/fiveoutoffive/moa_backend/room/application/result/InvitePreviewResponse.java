package com.fiveoutoffive.moa_backend.room.application.result;

import com.fiveoutoffive.moa_backend.global.infrastructure.s3.CdnMappable;

public record InvitePreviewResponse(Long roomId, String name, String description, String imageUrl, long memberCount, String myStatus) implements CdnMappable {
}
