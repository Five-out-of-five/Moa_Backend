package com.fiveoutoffive.moa_backend.mediation.application.result;

import com.fiveoutoffive.moa_backend.ideacard.application.result.IdeaCardDetailResponse;
import com.fiveoutoffive.moa_backend.mediation.domain.model.ConflictStatus;
import com.fiveoutoffive.moa_backend.mediation.domain.model.DraftCardType;
import com.fiveoutoffive.moa_backend.mediation.domain.model.DraftStatus;

public record ConflictDetailResponse(Long conflictId, ConflictStatus status, String reason, IdeaCardDetailResponse cardA, IdeaCardDetailResponse cardB, MyDraft myDraft, Long adoptedCardId) {

    public record MyDraft(Long draftId, DraftCardType cardType, String title, String content, DraftStatus status) {
    }
}
