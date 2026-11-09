package com.fiveoutoffive.moa_backend.account.infrastructure.adapter;

import com.fiveoutoffive.moa_backend.account.domain.model.AccountDailyActivity;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountDailyActivityRepository;
import com.fiveoutoffive.moa_backend.auth.exception.AuthErrorCode;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.port.out.AccountGuardPort;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.RedisKeys;
import java.time.Duration;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 정지 회원 차단과 일별 접속 기록. Redis 로 DB 조회를 줄이며, Redis 장애 시에는 요청을 막지 않는다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountGuardAdapter implements AccountGuardPort {

    private static final Duration ACTIVE_MARK_TTL = Duration.ofHours(25);

    private final StringRedisTemplate redisTemplate;
    private final AccountDailyActivityRepository dailyActivityRepository;

    @Override
    public void onAuthenticatedRequest(Long accountId) {
        String suspendedUntil;
        boolean firstToday;
        LocalDate today = KstTime.todayKst();
        try {
            suspendedUntil = redisTemplate.opsForValue().get(RedisKeys.SUSPENDED_PREFIX + accountId);
            firstToday = suspendedUntil == null && Boolean.TRUE.equals(redisTemplate.opsForValue()
                    .setIfAbsent(RedisKeys.DAILY_ACTIVE_PREFIX + today + ":" + accountId, "1", ACTIVE_MARK_TTL));
        } catch (Exception e) {
            log.warn("[AccountGuard] Redis 조회 실패: {}", e.getMessage());
            return;
        }
        if (suspendedUntil != null) {
            throw new BusinessException(AuthErrorCode.AUTH_ACCOUNT_SUSPENDED, suspendedUntil);
        }
        if (firstToday) {
            dailyActivityRepository.save(AccountDailyActivity.builder().accountId(accountId).activityDate(today).build());
        }
    }
}
