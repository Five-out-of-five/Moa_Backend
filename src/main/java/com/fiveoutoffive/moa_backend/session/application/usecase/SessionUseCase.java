package com.fiveoutoffive.moa_backend.session.application.usecase;

import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.session.application.command.SessionStartCommand;
import com.fiveoutoffive.moa_backend.session.application.result.SessionResponse;
import com.fiveoutoffive.moa_backend.session.application.result.SessionSummaryResponse;
import org.springframework.data.domain.Pageable;

public interface SessionUseCase {

    SessionResponse start(Long accountId, Long roomId, SessionStartCommand command);

    SessionResponse getCurrent(Long accountId, Long roomId);

    SessionResponse close(Long accountId, Long roomId, Long sessionId);

    PageResponse<SessionSummaryResponse> getClosed(Long accountId, Long roomId, Pageable pageable);

    SessionResponse getDetail(Long accountId, Long roomId, Long sessionId);
}
