package com.fiveoutoffive.moa_backend.ideacard.application.result;

import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskStatus;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import java.time.LocalDateTime;
import java.util.List;

/** content: 카드 본문(최대 1000자, 없으면 null). 보드 카드에 내용을 보여 주기 위해 목록에도 담는다. */
public record IdeaCardSummaryResponse(Long cardId, CardType cardType, String title, String content, MemberRef writer, Integer sortOrder, Long version, Boolean isAiGenerated, long commentCount, long fileCount, List<ReactionSummaryResponse> reactions, ConvertedTask convertedTask, LinkedVote linkedVote, long conflictCount, LocalDateTime createdAt) {

    public record ConvertedTask(Long taskId, TaskStatus status) {
    }

    public record LinkedVote(Long voteId, VoteStatus status) {
    }
}
