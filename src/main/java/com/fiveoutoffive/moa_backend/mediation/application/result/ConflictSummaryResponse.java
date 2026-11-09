package com.fiveoutoffive.moa_backend.mediation.application.result;

import com.fiveoutoffive.moa_backend.mediation.domain.model.ConflictStatus;
import java.time.LocalDateTime;

public record ConflictSummaryResponse(Long conflictId, ConflictStatus status, CardRef cardA, CardRef cardB, LocalDateTime detectedAt) {

    public record CardRef(Long cardId, String title) {
    }
}
