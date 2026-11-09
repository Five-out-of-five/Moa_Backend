package com.fiveoutoffive.moa_backend.admin.application.result;

import com.fiveoutoffive.moa_backend.account.domain.model.AccountRole;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountStatus;
import com.fiveoutoffive.moa_backend.account.domain.model.SocialProvider;
import java.time.LocalDateTime;

public record AdminAccountResponse(Long accountId, String name, String email, SocialProvider provider, AccountRole role, AccountStatus status, LocalDateTime suspendedUntil, LocalDateTime createdAt, LocalDateTime lastLoginAt) {
}
