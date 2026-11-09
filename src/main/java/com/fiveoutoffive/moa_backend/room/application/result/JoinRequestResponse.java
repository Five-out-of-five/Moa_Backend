package com.fiveoutoffive.moa_backend.room.application.result;

import com.fiveoutoffive.moa_backend.room.domain.model.JoinRequestStatus;
import java.time.LocalDateTime;

public record JoinRequestResponse(Long requestId, Long roomId, Requester requester, JoinRequestStatus status, LocalDateTime requestedAt, LocalDateTime expiresAt, LocalDateTime processedAt) {

    public record Requester(Long accountId, String name) {
    }
}
