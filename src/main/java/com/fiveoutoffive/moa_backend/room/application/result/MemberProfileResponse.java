package com.fiveoutoffive.moa_backend.room.application.result;

import com.fiveoutoffive.moa_backend.global.infrastructure.s3.CdnMappable;
import com.fiveoutoffive.moa_backend.global.type.MemberStatus;
import com.fiveoutoffive.moa_backend.mood.domain.model.Mood;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberRole;

public record MemberProfileResponse(Long accountId, MemberStatus memberStatus, String displayName, String profileImageUrl, String positionLabel, RoomMemberRole role, boolean online, LatestMood latestMood, boolean canStartDirectMessage) implements CdnMappable {

    public record LatestMood(Mood mood, String comment) {
    }
}
