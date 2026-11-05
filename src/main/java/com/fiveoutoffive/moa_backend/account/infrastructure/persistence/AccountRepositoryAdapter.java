package com.fiveoutoffive.moa_backend.account.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.account.infrastructure.mapper.AccountMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

    private final SpringDataAccountRepository springDataRepository;
    private final AccountMapper accountMapper;

    @Override
    public Account save(Account account) {
        AccountJpaEntity saved = springDataRepository.save(accountMapper.toJpaEntity(account));
        return accountMapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(Long id) {
        return springDataRepository.findById(id)
                .map(accountMapper::toDomain);
    }
}
