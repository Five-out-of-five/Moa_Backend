package com.fiveoutoffive.moa_backend.ideacard.application.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fiveoutoffive.moa_backend.file.application.result.FileResponse;
import com.fiveoutoffive.moa_backend.mediation.domain.model.ConflictStatus;
import java.time.LocalDateTime;
import java.util.List;

public record IdeaCardDetailResponse(@JsonUnwrapped @JsonIgnoreProperties("reactions") IdeaCardSummaryResponse summary, String content, Long sessionId, boolean sessionClosed, List<FileResponse> files, List<ReactionDetailResponse> reactions, List<ConflictRef> conflicts, LocalDateTime editedAt) {

    public record ConflictRef(Long conflictId, Long otherCardId, ConflictStatus status) {
    }
}
