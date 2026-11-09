package com.fiveoutoffive.moa_backend.room.application.result;

import com.fiveoutoffive.moa_backend.global.infrastructure.s3.CdnMappable;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberRole;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomStatus;
import java.time.LocalDateTime;

public record RoomDetailResponse(Long roomId, String name, String description, String imageUrl, RoomStatus status, long memberCount, int maxMemberCount, MyMembership myMembership, boolean needsProfileSetup) implements CdnMappable {

    public record MyMembership(RoomMemberRole role, String nickname, String displayName, String profileImageUrl, String positionLabel, Boolean notificationMuted, LocalDateTime joinedAt) implements CdnMappable {
    }
}
