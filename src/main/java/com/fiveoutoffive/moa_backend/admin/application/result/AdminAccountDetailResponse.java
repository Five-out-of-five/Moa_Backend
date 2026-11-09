package com.fiveoutoffive.moa_backend.admin.application.result;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fiveoutoffive.moa_backend.account.domain.model.SuspensionPeriod;
import java.time.LocalDateTime;
import java.util.List;

public record AdminAccountDetailResponse(@JsonUnwrapped AdminAccountResponse account, long roomCount, long ownedRoomCount, LocalDateTime reportBlockedUntil, List<Suspension> suspensions) {

    public record Suspension(Long suspensionId, SuspensionPeriod period, String reason, LocalDateTime startedAt, LocalDateTime endsAt, LocalDateTime releasedAt, AdminRef admin) {
    }

    public record AdminRef(Long accountId, String name) {
    }
}
