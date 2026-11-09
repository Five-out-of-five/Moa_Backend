package com.fiveoutoffive.moa_backend.room.application.result;

import com.fiveoutoffive.moa_backend.global.infrastructure.s3.CdnMappable;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberRole;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomStatus;

public record RoomSummaryResponse(Long roomId, String name, String imageUrl, RoomMemberRole myRole, long memberCount, RoomStatus status) implements CdnMappable {
}
