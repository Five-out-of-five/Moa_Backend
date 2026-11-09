package com.fiveoutoffive.moa_backend.auth.application.service;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountRole;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountStatus;
import com.fiveoutoffive.moa_backend.account.domain.model.SocialProvider;
import com.fiveoutoffive.moa_backend.account.domain.model.ToneMode;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.auth.application.command.SignupCommand;
import com.fiveoutoffive.moa_backend.auth.application.result.IssuedTokens;
import com.fiveoutoffive.moa_backend.auth.application.result.OAuthLoginResult;
import com.fiveoutoffive.moa_backend.auth.application.result.SignupTicketResponse;
import com.fiveoutoffive.moa_backend.auth.application.result.TokenResponse;
import com.fiveoutoffive.moa_backend.auth.application.usecase.AuthUseCase;
import com.fiveoutoffive.moa_backend.auth.exception.AuthErrorCode;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.security.GlobalJwtProvider;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.RedisKeys;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 소셜 로그인·가입·토큰 재발급. 리프레시 토큰은 Redis 에 계정당 1개 저장하고 재발급 때마다 교체한다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private static final Duration GRACE_TTL = Duration.ofSeconds(10);
    private static final Duration LOCK_TTL = Duration.ofSeconds(5);
    private static final int LOCK_WAIT_RETRIES = 10;
    private static final long LOCK_WAIT_MILLIS = 100;
    private static final String TICKET_SEPARATOR = "\n";

    private final AccountRepository accountRepository;
    private final GlobalJwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;
    private final RealtimePublisher realtimePublisher;

    @Transactional
    @Override
    public OAuthLoginResult handleOAuthLogin(SocialProvider provider, String providerUid, String email, String name) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessException(AuthErrorCode.AUTH_EMAIL_NOT_PROVIDED);
        }
        Optional<Account> found = accountRepository.findByProviderAndProviderUid(provider, providerUid);
        if (found.isEmpty()) {
            if (accountRepository.existsByEmail(email)) {
                throw new BusinessException(AuthErrorCode.AUTH_EMAIL_ALREADY_REGISTERED);
            }
            String ticket = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set(RedisKeys.SIGNUP_TICKET_PREFIX + ticket,
                    String.join(TICKET_SEPARATOR, provider.name(), providerUid, email, name == null ? "" : name),
                    jwtProvider.signupTicketTtl());
            return new OAuthLoginResult("/signup?ticket=" + ticket, null);
        }

        Account account = found.get();
        if (account.getStatus() == AccountStatus.WITHDRAWN) {
            throw new BusinessException(AuthErrorCode.AUTH_REJOIN_RESTRICTED);
        }
        ensureNotSuspended(account);
        accountRepository.save(account.toBuilder().lastLoginAt(KstTime.nowUtc()).build());
        return new OAuthLoginResult("/auth/callback", issueRefreshToken(account));
    }

    @Override
    public SignupTicketResponse getSignupTicket(String ticket) {
        String[] parts = readTicket(ticket);
        return new SignupTicketResponse(parts[0], parts[2], parts[3]);
    }

    @Transactional
    @Override
    public IssuedTokens signup(SignupCommand command) {
        String[] parts = readTicket(command.ticket());
        if (!Boolean.TRUE.equals(command.termsAgreed()) || !Boolean.TRUE.equals(command.privacyAgreed())) {
            throw new BusinessException(AuthErrorCode.AUTH_TERMS_NOT_AGREED);
        }
        if (accountRepository.existsByEmail(parts[2])) {
            throw new BusinessException(AuthErrorCode.AUTH_EMAIL_ALREADY_REGISTERED);
        }
        LocalDateTime now = KstTime.nowUtc();
        Account account = accountRepository.save(Account.builder()
                .provider(SocialProvider.valueOf(parts[0]))
                .providerUid(parts[1])
                .email(parts[2])
                .name(command.name())
                .role(AccountRole.USER)
                .status(AccountStatus.ACTIVE)
                .toneMode(ToneMode.POLISH)
                .termsAgreedAt(now)
                .privacyAgreedAt(now)
                .lastLoginAt(now)
                .build());
        redisTemplate.delete(RedisKeys.SIGNUP_TICKET_PREFIX + command.ticket());
        return new IssuedTokens(accessToken(account), issueRefreshToken(account));
    }

    @Override
    public IssuedTokens refresh(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new BusinessException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID);
        }
        String subject = parseSubject(refreshToken);
        String graceKey = RedisKeys.REFRESH_GRACE_PREFIX + sha256(refreshToken);
        String lockKey = RedisKeys.REFRESH_LOCK_PREFIX + subject;

        Optional<IssuedTokens> cached = readGrace(graceKey);
        if (cached.isPresent()) {
            return cached.get();
        }
        // 동시 요청은 먼저 잠근 쪽이 교체하고, 나머지는 그 결과를 유예 키에서 읽는다.
        if (!Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_TTL))) {
            return waitForGrace(graceKey);
        }
        try {
            String stored = redisTemplate.opsForValue().get(RedisKeys.REFRESH_TOKEN_PREFIX + subject);
            if (!refreshToken.equals(stored)) {
                throw new BusinessException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID);
            }
            Account account = accountRepository.findById(Long.valueOf(subject))
                    .orElseThrow(() -> new BusinessException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID));
            ensureNotSuspended(account);
            if (account.getStatus() == AccountStatus.WITHDRAWN) {
                throw new BusinessException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID);
            }
            IssuedTokens issued = new IssuedTokens(accessToken(account), issueRefreshToken(account));
            redisTemplate.opsForValue().set(graceKey, encodeGrace(issued), GRACE_TTL);
            return issued;
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public void logout(String refreshToken) {
        String subject = subjectEvenIfExpired(refreshToken);
        if (subject == null) {
            return;
        }
        redisTemplate.delete(RedisKeys.REFRESH_TOKEN_PREFIX + subject);
        realtimePublisher.disconnectUser(subject);
    }

    private void ensureNotSuspended(Account account) {
        if (account.getStatus() != AccountStatus.SUSPENDED) {
            return;
        }
        if (account.getSuspendedUntil() != null && account.getSuspendedUntil().isBefore(KstTime.nowUtc())) {
            return; // 기간이 지났고 스케줄러가 아직 돌지 않은 경우
        }
        throw new BusinessException(AuthErrorCode.AUTH_ACCOUNT_SUSPENDED, account.getSuspendedUntil());
    }

    private String[] readTicket(String ticket) {
        String value = redisTemplate.opsForValue().get(RedisKeys.SIGNUP_TICKET_PREFIX + ticket);
        if (value == null) {
            throw new BusinessException(AuthErrorCode.AUTH_SIGNUP_TICKET_INVALID);
        }
        return value.split(TICKET_SEPARATOR, 4);
    }

    private TokenResponse accessToken(Account account) {
        String token = jwtProvider.createAccessToken(String.valueOf(account.getId()), account.getRole().name());
        LocalDateTime expiresAt = LocalDateTime.ofInstant(jwtProvider.getExpiration(token).toInstant(), ZoneOffset.UTC);
        return new TokenResponse(token, expiresAt);
    }

    private String issueRefreshToken(Account account) {
        String subject = String.valueOf(account.getId());
        String token = jwtProvider.createRefreshToken(subject);
        redisTemplate.opsForValue().set(RedisKeys.REFRESH_TOKEN_PREFIX + subject, token, jwtProvider.refreshTokenTtl());
        return token;
    }

    private String parseSubject(String token) {
        try {
            return jwtProvider.getSubject(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID);
        }
    }

    private String subjectEvenIfExpired(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            return jwtProvider.getSubject(token);
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private IssuedTokens waitForGrace(String graceKey) {
        for (int i = 0; i < LOCK_WAIT_RETRIES; i++) {
            try {
                Thread.sleep(LOCK_WAIT_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            Optional<IssuedTokens> cached = readGrace(graceKey);
            if (cached.isPresent()) {
                return cached.get();
            }
        }
        throw new BusinessException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID);
    }

    private Optional<IssuedTokens> readGrace(String graceKey) {
        String value = redisTemplate.opsForValue().get(graceKey);
        if (value == null) {
            return Optional.empty();
        }
        String[] parts = value.split(TICKET_SEPARATOR, 3);
        return Optional.of(new IssuedTokens(new TokenResponse(parts[0], LocalDateTime.parse(parts[1])), parts[2]));
    }

    private String encodeGrace(IssuedTokens issued) {
        return String.join(TICKET_SEPARATOR, issued.token().accessToken(),
                issued.token().accessTokenExpiresAt().toString(), issued.refreshToken());
    }

    private String sha256(String value) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
