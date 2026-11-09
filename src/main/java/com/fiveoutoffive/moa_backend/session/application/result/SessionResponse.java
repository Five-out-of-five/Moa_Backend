package com.fiveoutoffive.moa_backend.session.application.result;

import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.session.domain.model.SessionStatus;
import java.time.LocalDateTime;

public record SessionResponse(Long sessionId, Integer sessionNo, String title, SessionStatus status, LocalDateTime deadlineAt, MemberRef startedBy, LocalDateTime startedAt, LocalDateTime closedAt, Long cardCount, Long participantCount) {
}
