package com.fiveoutoffive.moa_backend.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAccountSuspensionRepository extends JpaRepository<AccountSuspensionJpaEntity, Long> {
}
