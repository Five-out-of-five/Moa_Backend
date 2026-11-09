package com.fiveoutoffive.moa_backend.account.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.account.domain.model.AccountSuspension;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountSuspensionRepository;
import com.fiveoutoffive.moa_backend.account.infrastructure.mapper.AccountSuspensionMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountSuspensionRepositoryAdapter implements AccountSuspensionRepository {

    private final SpringDataAccountSuspensionRepository springDataRepository;
    private final AccountSuspensionMapper accountSuspensionMapper;

    @Override
    public AccountSuspension save(AccountSuspension accountSuspension) {
        AccountSuspensionJpaEntity saved = springDataRepository.save(accountSuspensionMapper.toJpaEntity(accountSuspension));
        return accountSuspensionMapper.toDomain(saved);
    }

    @Override
    public Optional<AccountSuspension> findById(Long id) {
        return springDataRepository.findById(id)
                .map(accountSuspensionMapper::toDomain);
    }

    @Override
    public List<AccountSuspension> findByAccountIdOrderByStartedAtDesc(Long accountId) {
        return springDataRepository.findByAccountIdOrderByStartedAtDesc(accountId).stream().map(accountSuspensionMapper::toDomain).toList();
    }

    @Override
    public Optional<AccountSuspension> findFirstByAccountIdAndReleasedAtIsNullOrderByStartedAtDesc(Long accountId) {
        return springDataRepository.findFirstByAccountIdAndReleasedAtIsNullOrderByStartedAtDesc(accountId).map(accountSuspensionMapper::toDomain);
    }
}
