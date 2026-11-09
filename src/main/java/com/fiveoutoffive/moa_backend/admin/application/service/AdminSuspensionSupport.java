package com.fiveoutoffive.moa_backend.admin.application.service;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountRole;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountStatus;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountSuspension;
import com.fiveoutoffive.moa_backend.account.domain.model.SuspensionPeriod;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountSuspensionRepository;
import com.fiveoutoffive.moa_backend.admin.exception.AdminErrorCode;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.RedisKeys;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 회원 정지. 회원 관리와 신고 조치가 함께 쓴다. */
@Component
@RequiredArgsConstructor
public class AdminSuspensionSupport {

    private final AccountRepository accountRepository;
    private final AccountSuspensionRepository suspensionRepository;
    private final AdminAuditRecorder audit;
    private final NotificationSender notificationSender;
    private final StringRedisTemplate redisTemplate;
    private final RealtimePublisher realtime;

    public void suspend(Long adminId, Account account, SuspensionPeriod period, String reason, Long reportId) {
        if (account.getRole() == AccountRole.ADMIN) {
            throw new BusinessException(AdminErrorCode.ADMIN_CANNOT_SUSPEND_ADMIN);
        }
        if (account.getStatus() == AccountStatus.WITHDRAWN) {
            throw new BusinessException(AdminErrorCode.ADMIN_ACCOUNT_WITHDRAWN);
        }
        if (account.getStatus() == AccountStatus.SUSPENDED) {
            throw new BusinessException(AdminErrorCode.ADMIN_ALREADY_SUSPENDED);
        }
        LocalDateTime now = KstTime.nowUtc();
        LocalDateTime until = switch (period) {
            case DAYS_3 -> now.plusDays(3);
            case DAYS_7 -> now.plusDays(7);
            case DAYS_30 -> now.plusDays(30);
            case PERMANENT -> null;
        };
        accountRepository.save(account.toBuilder().status(AccountStatus.SUSPENDED).suspendedUntil(until).build());
        suspensionRepository.save(AccountSuspension.builder().accountId(account.getId()).adminAccountId(adminId).reportId(reportId).period(period).reason(reason).startedAt(now).endsAt(until).build());
        String mark = until == null ? "PERMANENT" : until.toString();
        if (until == null) {
            redisTemplate.opsForValue().set(RedisKeys.SUSPENDED_PREFIX + account.getId(), mark);
        } else {
            redisTemplate.opsForValue().set(RedisKeys.SUSPENDED_PREFIX + account.getId(), mark, Duration.between(now, until));
        }
        // 리프레시 토큰을 지워 즉시 로그아웃시킨다.
        redisTemplate.delete(RedisKeys.REFRESH_TOKEN_PREFIX + account.getId());
        realtime.disconnectUser(String.valueOf(account.getId()));
        notificationSender.send(null, adminId, NotificationType.ACCOUNT_SUSPENDED, List.of(account.getId()), "계정이 정지되었습니다", reason, "ACCOUNT", account.getId());
        audit.record(adminId, "ACCOUNT_SUSPEND", "ACCOUNT", account.getId(), Map.of("period", period, "reason", reason));
    }
}
