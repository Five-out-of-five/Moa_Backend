package com.fiveoutoffive.moa_backend.room.application.result;

import com.fiveoutoffive.moa_backend.global.infrastructure.s3.CdnMappable;
import com.fiveoutoffive.moa_backend.mood.domain.model.Mood;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberRole;

public record MemberResponse(Long accountId, String displayName, String profileImageUrl, String positionLabel, RoomMemberRole role, boolean online, TodayMood todayMood) implements CdnMappable {

    public record TodayMood(Mood mood, String comment) {
    }
}
