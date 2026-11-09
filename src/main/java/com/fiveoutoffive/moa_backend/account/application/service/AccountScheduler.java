package com.fiveoutoffive.moa_backend.account.application.service;

import com.fiveoutoffive.moa_backend.account.domain.model.AccountStatus;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountSuspensionRepository;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.RedisKeys;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 정지 기간 만료 해제, 탈퇴 30일 후 개인정보 파기. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountScheduler {

    private final AccountRepository accountRepository;
    private final AccountSuspensionRepository suspensionRepository;
    private final StringRedisTemplate redisTemplate;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void releaseExpiredSuspensions() {
        LocalDateTime now = KstTime.nowUtc();
        accountRepository.findByStatusAndSuspendedUntilBefore(AccountStatus.SUSPENDED, now).forEach(account -> {
            accountRepository.save(account.toBuilder().status(AccountStatus.ACTIVE).suspendedUntil(null).build());
            suspensionRepository.findFirstByAccountIdAndReleasedAtIsNullOrderByStartedAtDesc(account.getId())
                    .ifPresent(suspension -> suspensionRepository.save(suspension.toBuilder().releasedAt(now).build()));
            redisTemplate.delete(RedisKeys.SUSPENDED_PREFIX + account.getId());
        });
    }

    @Scheduled(cron = "0 0 19 * * *", zone = "UTC")
    @Transactional
    public void purgeWithdrawnAccounts() {
        accountRepository.findByPurgeAtBeforeAndEmailIsNotNull(KstTime.nowUtc()).forEach(account ->
                accountRepository.save(account.toBuilder().providerUid(null).email(null).name(null).build()));
    }
}
