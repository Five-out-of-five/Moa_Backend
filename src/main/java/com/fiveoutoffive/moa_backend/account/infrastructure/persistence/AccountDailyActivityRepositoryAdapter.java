package com.fiveoutoffive.moa_backend.account.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.account.domain.model.AccountDailyActivity;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountDailyActivityRepository;
import com.fiveoutoffive.moa_backend.account.infrastructure.mapper.AccountDailyActivityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountDailyActivityRepositoryAdapter implements AccountDailyActivityRepository {

    private final SpringDataAccountDailyActivityRepository springDataRepository;
    private final AccountDailyActivityMapper accountDailyActivityMapper;

    @Override
    public AccountDailyActivity save(AccountDailyActivity accountDailyActivity) {
        AccountDailyActivityJpaEntity saved = springDataRepository.save(accountDailyActivityMapper.toJpaEntity(accountDailyActivity));
        return accountDailyActivityMapper.toDomain(saved);
    }
}
