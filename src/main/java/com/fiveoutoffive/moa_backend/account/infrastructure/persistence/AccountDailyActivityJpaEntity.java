package com.fiveoutoffive.moa_backend.account.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 일별 접속 기록 (서비스 통계 활성 사용자 수 추이) (account_daily_activity). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "account_daily_activity")
@IdClass(AccountDailyActivityJpaId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountDailyActivityJpaEntity {

    @Id
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Id
    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public AccountDailyActivityJpaEntity(
            Long accountId,
            LocalDate activityDate
    ) {
        this.accountId = accountId;
        this.activityDate = activityDate;
    }
}
