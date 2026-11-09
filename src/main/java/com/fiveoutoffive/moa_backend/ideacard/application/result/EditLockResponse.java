package com.fiveoutoffive.moa_backend.ideacard.application.result;

import java.time.LocalDateTime;

public record EditLockResponse(LockedBy lockedBy, LocalDateTime expiresAt) {

    public record LockedBy(Long accountId, String displayName) {
    }
}
