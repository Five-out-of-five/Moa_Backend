package com.fiveoutoffive.moa_backend.briefing.application.usecase;

import com.fiveoutoffive.moa_backend.briefing.application.result.BriefingResponse;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventType;

public interface BriefingUseCase {

    BriefingResponse get(Long accountId, Long roomId);

    BriefingResponse checkAll(Long accountId, Long roomId);

    BriefingResponse dismiss(Long accountId, Long roomId, BoardEventType eventType);
}
