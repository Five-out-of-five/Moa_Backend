package com.fiveoutoffive.moa_backend.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAccountDailyActivityRepository extends JpaRepository<AccountDailyActivityJpaEntity, AccountDailyActivityJpaId> {
}
