package com.fiveoutoffive.moa_backend.mood.application.result;

import com.fiveoutoffive.moa_backend.global.infrastructure.s3.CdnMappable;
import com.fiveoutoffive.moa_backend.mood.domain.model.Mood;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record TeamMoodResponse(Map<Mood, Long> distribution, List<MemberMood> members, List<TrendPoint> trend) {

    public record MemberMood(Long accountId, String displayName, String profileImageUrl, Mood mood, String comment, LocalDate recordedDate) implements CdnMappable {
    }

    public record TrendPoint(LocalDate date, Map<Mood, Long> counts) {
    }
}
