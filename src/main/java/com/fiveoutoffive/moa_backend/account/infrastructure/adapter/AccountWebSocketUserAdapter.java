package com.fiveoutoffive.moa_backend.account.infrastructure.adapter;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountStatus;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.global.websocket.WebSocketUserPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** JWT subject 는 계정 ID 다. 존재하고 탈퇴하지 않은 계정만 접속할 수 있다. */
@Component
@RequiredArgsConstructor
public class AccountWebSocketUserAdapter implements WebSocketUserPort {

    private final AccountRepository accountRepository;

    @Override
    public Optional<Long> findUserIdByPrincipal(String principal) {
        try {
            return accountRepository.findById(Long.valueOf(principal))
                    .filter(account -> account.getStatus() != AccountStatus.WITHDRAWN)
                    .map(Account::getId);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
