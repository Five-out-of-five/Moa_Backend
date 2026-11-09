package com.fiveoutoffive.moa_backend.account.infrastructure.persistence;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataAccountDailyActivityRepository extends JpaRepository<AccountDailyActivityJpaEntity, AccountDailyActivityJpaId> {

    @Query("select count(a) from AccountDailyActivityJpaEntity a where a.activityDate >= :from and a.activityDate <= :to")
    long countBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
