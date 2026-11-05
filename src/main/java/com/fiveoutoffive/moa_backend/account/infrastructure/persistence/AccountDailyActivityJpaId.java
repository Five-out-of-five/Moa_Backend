package com.fiveoutoffive.moa_backend.account.infrastructure.persistence;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** account_daily_activity 복합 PK (account_id, activity_date) */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class AccountDailyActivityJpaId implements Serializable {

    private Long accountId;
    private LocalDate activityDate;
}
