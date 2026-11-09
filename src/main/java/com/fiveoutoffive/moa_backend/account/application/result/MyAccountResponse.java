package com.fiveoutoffive.moa_backend.account.application.result;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountRole;
import com.fiveoutoffive.moa_backend.account.domain.model.SocialProvider;
import com.fiveoutoffive.moa_backend.account.domain.model.ToneMode;
import java.time.LocalDateTime;

public record MyAccountResponse(Long accountId, String name, String email, SocialProvider provider, AccountRole role, ToneMode toneMode, Long lastRoomId, LocalDateTime createdAt) {

    public static MyAccountResponse from(Account a) {
        return new MyAccountResponse(a.getId(), a.getName(), a.getEmail(), a.getProvider(), a.getRole(), a.getToneMode(), a.getLastRoomId(), a.getCreatedAt());
    }
}
