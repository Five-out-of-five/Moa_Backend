package com.fiveoutoffive.moa_backend.mediation.application.result;

import com.fiveoutoffive.moa_backend.mediation.domain.model.DraftCardType;
import com.fiveoutoffive.moa_backend.mediation.domain.model.DraftStatus;

public record CompromiseDraftResponse(Long draftId, DraftCardType cardType, String title, String content, DraftStatus status) {
}
