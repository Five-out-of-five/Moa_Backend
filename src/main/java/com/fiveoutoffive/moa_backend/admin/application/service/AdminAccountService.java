package com.fiveoutoffive.moa_backend.admin.application.service;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountRole;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountStatus;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountSuspension;
import com.fiveoutoffive.moa_backend.account.domain.model.SocialProvider;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountSuspensionRepository;
import com.fiveoutoffive.moa_backend.account.exception.AccountErrorCode;
import com.fiveoutoffive.moa_backend.admin.application.command.RoleChangeCommand;
import com.fiveoutoffive.moa_backend.admin.application.command.SuspendCommand;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminAccountDetailResponse;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminAccountResponse;
import com.fiveoutoffive.moa_backend.admin.application.usecase.AdminAccountUseCase;
import com.fiveoutoffive.moa_backend.admin.exception.AdminErrorCode;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.global.util.RedisKeys;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMember;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberStatus;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomMemberRepository;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminAccountService implements AdminAccountUseCase {

    private final AccountRepository accountRepository;
    private final AccountSuspensionRepository suspensionRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomRepository roomRepository;
    private final AdminAuditRecorder audit;
    private final NotificationSender notificationSender;
    private final StringRedisTemplate redisTemplate;
    private final RealtimePublisher realtime;
    private final AdminSuspensionSupport suspensionSupport;

    @Transactional(readOnly = true)
    @Override
    public PageResponse<AdminAccountResponse> getAccounts(String keyword, SocialProvider provider, AccountStatus status, AccountRole role, LocalDate joinedFrom, LocalDate joinedTo, Pageable pageable) {
        return Pages.response(accountRepository.search(keyword, provider, status, role, joinedFrom == null ? null : KstTime.startOfDayUtc(joinedFrom), joinedTo == null ? null : KstTime.startOfDayUtc(joinedTo.plusDays(1)), pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    @Override
    public AdminAccountDetailResponse getAccount(Long accountId) {
        return detail(require(accountId));
    }

    @Override
    public AdminAccountDetailResponse suspend(Long adminId, Long accountId, SuspendCommand command) {
        suspensionSupport.suspend(adminId, require(accountId), command.period(), command.reason(), command.reportId());
        return detail(require(accountId));
    }

    @Override
    public AdminAccountDetailResponse release(Long adminId, Long accountId) {
        Account account = require(accountId);
        if (account.getStatus() != AccountStatus.SUSPENDED) {
            throw new BusinessException(AdminErrorCode.ADMIN_NOT_SUSPENDED);
        }
        accountRepository.save(account.toBuilder().status(AccountStatus.ACTIVE).suspendedUntil(null).build());
        suspensionRepository.findFirstByAccountIdAndReleasedAtIsNullOrderByStartedAtDesc(accountId).ifPresent(s -> suspensionRepository.save(s.toBuilder().releasedAt(KstTime.nowUtc()).releasedByAccountId(adminId).build()));
        redisTemplate.delete(RedisKeys.SUSPENDED_PREFIX + accountId);
        notificationSender.send(null, adminId, NotificationType.ACCOUNT_SUSPENDED, List.of(accountId), "계정 정지가 해제되었습니다", null, "ACCOUNT", accountId);
        audit.record(adminId, "ACCOUNT_RELEASE", "ACCOUNT", accountId, null);
        return detail(require(accountId));
    }

    @Override
    public AdminAccountDetailResponse changeRole(Long adminId, Long accountId, RoleChangeCommand command) {
        if (adminId.equals(accountId)) {
            throw new BusinessException(AdminErrorCode.ADMIN_CANNOT_CHANGE_OWN_ROLE);
        }
        Account account = require(accountId);
        if (account.getStatus() == AccountStatus.WITHDRAWN) {
            throw new BusinessException(AdminErrorCode.ADMIN_ACCOUNT_WITHDRAWN);
        }
        if (account.getRole() == AccountRole.ADMIN && command.role() == AccountRole.USER && accountRepository.countByRoleAndDeletedAtIsNull(AccountRole.ADMIN) <= 1) {
            throw new BusinessException(AdminErrorCode.ADMIN_LAST_ADMIN);
        }
        accountRepository.save(account.toBuilder().role(command.role()).build());
        realtime.disconnectUser(String.valueOf(accountId));
        audit.record(adminId, "ROLE_CHANGE", "ACCOUNT", accountId, Map.of("role", command.role()));
        return detail(require(accountId));
    }

    private Account require(Long accountId) {
        return accountRepository.findById(accountId).orElseThrow(() -> new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND));
    }

    private AdminAccountResponse toResponse(Account a) {
        return new AdminAccountResponse(a.getId(), a.getName(), a.getEmail(), a.getProvider(), a.getRole(), a.getStatus(), a.getSuspendedUntil(), a.getCreatedAt(), a.getLastLoginAt());
    }

    private AdminAccountDetailResponse detail(Account a) {
        List<RoomMember> memberships = roomMemberRepository.findByAccountIdAndStatus(a.getId(), RoomMemberStatus.ACTIVE);
        Map<Long, String> names = new HashMap<>();
        List<AccountSuspension> suspensions = suspensionRepository.findByAccountIdOrderByStartedAtDesc(a.getId());
        accountRepository.findByIdIn(suspensions.stream().map(AccountSuspension::getAdminAccountId).collect(Collectors.toSet())).forEach(admin -> names.put(admin.getId(), admin.getName()));
        return new AdminAccountDetailResponse(toResponse(a), memberships.size(), roomRepository.countByCreatedByAccountIdAndDeletedAtIsNull(a.getId()), a.getReportBlockedUntil(),
                suspensions.stream().map(s -> new AdminAccountDetailResponse.Suspension(s.getId(), s.getPeriod(), s.getReason(), s.getStartedAt(), s.getEndsAt(), s.getReleasedAt(), new AdminAccountDetailResponse.AdminRef(s.getAdminAccountId(), names.get(s.getAdminAccountId())))).toList());
    }
}
