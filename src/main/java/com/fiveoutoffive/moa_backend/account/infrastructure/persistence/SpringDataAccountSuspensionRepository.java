package com.fiveoutoffive.moa_backend.account.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAccountSuspensionRepository extends JpaRepository<AccountSuspensionJpaEntity, Long> {

    List<AccountSuspensionJpaEntity> findByAccountIdOrderByStartedAtDesc(Long accountId);

    Optional<AccountSuspensionJpaEntity> findFirstByAccountIdAndReleasedAtIsNullOrderByStartedAtDesc(Long accountId);
}
