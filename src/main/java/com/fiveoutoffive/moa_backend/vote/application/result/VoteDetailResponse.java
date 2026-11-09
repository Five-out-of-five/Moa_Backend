package com.fiveoutoffive.moa_backend.vote.application.result;

import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteCloseReason;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteType;
import java.time.LocalDateTime;
import java.util.List;

public record VoteDetailResponse(Long voteId, String title, VoteType voteType, Integer maxSelectCount, Boolean anonymous, VoteStatus status, LocalDateTime deadlineAt, VoteCloseReason closeReason, MemberRef creator, SourceCard sourceCard, Long parentVoteId, Long version, long participantCount, long memberCount, List<MemberRef> participants, List<Long> myOptionIds, List<Option> options, boolean tied) {

    public record SourceCard(Long cardId, String title) {
    }

    public record Option(Long optionId, String content, Long voteCount, boolean top) {
    }
}
