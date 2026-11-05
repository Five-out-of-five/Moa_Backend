package com.fiveoutoffive.moa_backend.account.domain.model;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 일별 접속 기록 (서비스 통계 활성 사용자 수 추이) (account_daily_activity)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountDailyActivity {

    /** 회원 */
    private Long accountId;

    /** 접속 일자 (하루 1행, 그날 첫 요청 시 기록) */
    private LocalDate activityDate;

    private AccountDailyActivity(
            Long accountId,
            LocalDate activityDate
    ) {
        this.accountId = accountId;
        this.activityDate = activityDate;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static AccountDailyActivity reconstitute(
            Long accountId,
            LocalDate activityDate
    ) {
        return new AccountDailyActivity(accountId, activityDate);
    }
}
