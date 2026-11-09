package com.fiveoutoffive.moa_backend.vote.application.usecase;

import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.vote.application.command.BallotCommand;
import com.fiveoutoffive.moa_backend.vote.application.command.RevoteCommand;
import com.fiveoutoffive.moa_backend.vote.application.command.VoteCreateCommand;
import com.fiveoutoffive.moa_backend.vote.application.command.VoteUpdateCommand;
import com.fiveoutoffive.moa_backend.vote.application.result.VoteDetailResponse;
import com.fiveoutoffive.moa_backend.vote.application.result.VoteSummaryResponse;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import org.springframework.data.domain.Pageable;

public interface VoteUseCase {

    PageResponse<VoteSummaryResponse> getVotes(Long accountId, Long roomId, VoteStatus status, Pageable pageable);

    VoteDetailResponse create(Long accountId, Long roomId, VoteCreateCommand command);

    VoteDetailResponse getDetail(Long accountId, Long roomId, Long voteId);

    VoteDetailResponse update(Long accountId, Long roomId, Long voteId, VoteUpdateCommand command);

    void delete(Long accountId, Long roomId, Long voteId);

    VoteDetailResponse cast(Long accountId, Long roomId, Long voteId, BallotCommand command);

    VoteDetailResponse close(Long accountId, Long roomId, Long voteId);

    VoteDetailResponse revote(Long accountId, Long roomId, Long voteId, RevoteCommand command);
}
