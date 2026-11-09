package com.fiveoutoffive.moa_backend.vote.application.result;

import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteType;
import java.time.LocalDateTime;

public record VoteSummaryResponse(Long voteId, String title, VoteType voteType, VoteStatus status, LocalDateTime deadlineAt, long participantCount, long memberCount, boolean votedByMe, Long sourceCardId) {
}
