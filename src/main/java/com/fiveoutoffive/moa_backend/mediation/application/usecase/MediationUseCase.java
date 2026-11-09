package com.fiveoutoffive.moa_backend.mediation.application.usecase;

import com.fiveoutoffive.moa_backend.ideacard.application.result.IdeaCardDetailResponse;
import com.fiveoutoffive.moa_backend.mediation.application.command.DraftAdoptCommand;
import com.fiveoutoffive.moa_backend.mediation.application.result.CompromiseDraftResponse;
import com.fiveoutoffive.moa_backend.mediation.application.result.ConflictDetailResponse;
import com.fiveoutoffive.moa_backend.mediation.application.result.ConflictSummaryResponse;
import com.fiveoutoffive.moa_backend.mediation.domain.model.ConflictStatus;
import java.util.List;

public interface MediationUseCase {

    List<ConflictSummaryResponse> getConflicts(Long accountId, Long roomId, ConflictStatus status);

    ConflictDetailResponse getConflict(Long accountId, Long roomId, Long conflictId);

    ConflictSummaryResponse dismiss(Long accountId, Long roomId, Long conflictId);

    CompromiseDraftResponse createDraft(Long accountId, Long roomId, Long conflictId);

    IdeaCardDetailResponse adopt(Long accountId, Long roomId, Long conflictId, Long draftId, DraftAdoptCommand command);

    ConflictSummaryResponse discard(Long accountId, Long roomId, Long conflictId, Long draftId);
}
