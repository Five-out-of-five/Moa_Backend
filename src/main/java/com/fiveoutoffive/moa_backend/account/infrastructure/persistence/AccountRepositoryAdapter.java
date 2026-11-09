package com.fiveoutoffive.moa_backend.account.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountRole;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountStatus;
import com.fiveoutoffive.moa_backend.account.domain.model.SocialProvider;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.account.infrastructure.mapper.AccountMapper;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
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

    @Override
    public Optional<Account> findByProviderAndProviderUid(SocialProvider provider, String providerUid) {
        return springDataRepository.findByProviderAndProviderUid(provider, providerUid).map(accountMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataRepository.existsByEmail(email);
    }

    @Override
    public long countByRoleAndDeletedAtIsNull(AccountRole role) {
        return springDataRepository.countByRoleAndDeletedAtIsNull(role);
    }

    @Override
    public List<Account> findByIdIn(Collection<Long> ids) {
        return springDataRepository.findByIdIn(ids).stream().map(accountMapper::toDomain).toList();
    }

    @Override
    public List<Account> findByPurgeAtBeforeAndEmailIsNotNull(LocalDateTime time) {
        return springDataRepository.findByPurgeAtBeforeAndEmailIsNotNull(time).stream().map(accountMapper::toDomain).toList();
    }

    @Override
    public List<Account> findByStatusAndSuspendedUntilBefore(AccountStatus status, LocalDateTime time) {
        return springDataRepository.findByStatusAndSuspendedUntilBefore(status, time).stream().map(accountMapper::toDomain).toList();
    }

    @Override
    public Page<Account> search(@Param("keyword") String keyword, @Param("provider") SocialProvider provider, @Param("status") AccountStatus status, @Param("role") AccountRole role, @Param("joinedFrom") LocalDateTime joinedFrom, @Param("joinedTo") LocalDateTime joinedTo, Pageable pageable) {
        return springDataRepository.search(keyword, provider, status, role, joinedFrom, joinedTo, pageable).map(accountMapper::toDomain);
    }

    @Override
    public long countByCreatedAtBefore(LocalDateTime time) {
        return springDataRepository.countByCreatedAtBefore(time);
    }

    @Override
    public long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from, LocalDateTime to) {
        return springDataRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(from, to);
    }

    @Override
    public List<Long> findActiveIds() {
        return springDataRepository.findActiveIds();
    }
}
