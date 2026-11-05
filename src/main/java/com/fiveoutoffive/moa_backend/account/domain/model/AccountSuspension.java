package com.fiveoutoffive.moa_backend.account.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 계정 정지 이력 (삭제하지 않고 보관) (account_suspension)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountSuspension {

    /** PK */
    private Long id;

    /** 정지 대상 (NOT NULL) */
    private Long accountId;

    /** 처리한 서비스 관리자 (NOT NULL) */
    private Long adminAccountId;

    /** 신고 조치로 인한 정지면 해당 신고 */
    private Long reportId;

    /** 정지 기간 (NOT NULL) */
    private SuspensionPeriod period;

    /** 정지 사유 (NOT NULL) */
    private String reason;

    /** 정지 시작 (NOT NULL) */
    private LocalDateTime startedAt;

    /** 정지 종료 예정 (영구면 NULL) */
    private LocalDateTime endsAt;

    /** 해제 시각 (기간 만료 또는 수동 해제) */
    private LocalDateTime releasedAt;

    /** 수동 해제한 관리자 */
    private Long releasedByAccountId;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    private AccountSuspension(
            Long id,
            Long accountId,
            Long adminAccountId,
            Long reportId,
            SuspensionPeriod period,
            String reason,
            LocalDateTime startedAt,
            LocalDateTime endsAt,
            LocalDateTime releasedAt,
            Long releasedByAccountId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.accountId = accountId;
        this.adminAccountId = adminAccountId;
        this.reportId = reportId;
        this.period = period;
        this.reason = reason;
        this.startedAt = startedAt;
        this.endsAt = endsAt;
        this.releasedAt = releasedAt;
        this.releasedByAccountId = releasedByAccountId;
        this.createdAt = createdAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static AccountSuspension reconstitute(
            Long id,
            Long accountId,
            Long adminAccountId,
            Long reportId,
            SuspensionPeriod period,
            String reason,
            LocalDateTime startedAt,
            LocalDateTime endsAt,
            LocalDateTime releasedAt,
            Long releasedByAccountId,
            LocalDateTime createdAt
    ) {
        return new AccountSuspension(id, accountId, adminAccountId, reportId, period, reason, startedAt, endsAt, releasedAt, releasedByAccountId, createdAt);
    }
}
