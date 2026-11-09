package com.fiveoutoffive.moa_backend.account.application.service;

import com.fiveoutoffive.moa_backend.account.application.command.AccountUpdateCommand;
import com.fiveoutoffive.moa_backend.account.application.command.AiSettingsUpdateCommand;
import com.fiveoutoffive.moa_backend.account.application.command.LastRoomUpdateCommand;
import com.fiveoutoffive.moa_backend.account.application.command.WithdrawCommand;
import com.fiveoutoffive.moa_backend.account.application.result.MyAccountResponse;
import com.fiveoutoffive.moa_backend.account.application.result.WithdrawalCheckResponse;
import com.fiveoutoffive.moa_backend.account.application.usecase.AccountUseCase;
import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountStatus;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.account.exception.AccountErrorCode;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.RedisKeys;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import com.fiveoutoffive.moa_backend.room.domain.event.RoomMemberLeftEvent;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberRole;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberStatus;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomMemberRepository;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 내 계정 조회·수정·탈퇴. */
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements AccountUseCase {

    /** 탈퇴 화면에서 사용자가 그대로 입력해야 하는 문구 */
    public static final String WITHDRAW_CONFIRM_TEXT = "탈퇴합니다";
    private static final int PURGE_AFTER_DAYS = 30;

    private final AccountRepository accountRepository;
    private final RoomAccessService roomAccessService;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomRepository roomRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;
    private final RealtimePublisher realtimePublisher;

    @Transactional(readOnly = true)
    @Override
    public MyAccountResponse getMe(Long accountId) {
        return MyAccountResponse.from(require(accountId));
    }

    @Override
    public MyAccountResponse updateMe(Long accountId, AccountUpdateCommand command) {
        return MyAccountResponse.from(accountRepository.save(require(accountId).toBuilder().name(command.name()).build()));
    }

    @Override
    public MyAccountResponse updateAiSettings(Long accountId, AiSettingsUpdateCommand command) {
        return MyAccountResponse.from(accountRepository.save(require(accountId).toBuilder().toneMode(command.toneMode()).build()));
    }

    @Override
    public void updateLastRoom(Long accountId, LastRoomUpdateCommand command) {
        roomAccessService.requireMember(command.roomId(), accountId);
        accountRepository.save(require(accountId).toBuilder().lastRoomId(command.roomId()).build());
    }

    @Transactional(readOnly = true)
    @Override
    public WithdrawalCheckResponse checkWithdrawal(Long accountId) {
        List<WithdrawalCheckResponse.OwnedRoom> owned = ownedRooms(accountId);
        return new WithdrawalCheckResponse(owned.isEmpty(), owned);
    }

    @Override
    public void withdraw(Long accountId, WithdrawCommand command) {
        if (!WITHDRAW_CONFIRM_TEXT.equals(command.confirmText())) {
            throw new BusinessException(AccountErrorCode.ACCOUNT_WITHDRAW_CONFIRM_MISMATCH);
        }
        if (!ownedRooms(accountId).isEmpty()) {
            throw new BusinessException(AccountErrorCode.ACCOUNT_OWNED_ROOM_EXISTS);
        }
        LocalDateTime now = KstTime.nowUtc();
        Account account = require(accountId);
        accountRepository.save(account.toBuilder()
                .status(AccountStatus.WITHDRAWN)
                .deletedAt(now)
                .purgeAt(now.plusDays(PURGE_AFTER_DAYS))
                .build());
        roomMemberRepository.findByAccountIdAndStatus(accountId, RoomMemberStatus.ACTIVE).forEach(member -> {
            roomMemberRepository.save(member.toBuilder().status(RoomMemberStatus.LEFT).nickname(null).profileFileId(null).leftAt(now).build());
            eventPublisher.publishEvent(new RoomMemberLeftEvent(member.getRoomId(), accountId, RoomMemberStatus.LEFT));
        });
        redisTemplate.delete(RedisKeys.REFRESH_TOKEN_PREFIX + accountId);
        realtimePublisher.disconnectUser(String.valueOf(accountId));
    }

    private List<WithdrawalCheckResponse.OwnedRoom> ownedRooms(Long accountId) {
        return roomMemberRepository.findByAccountIdAndStatus(accountId, RoomMemberStatus.ACTIVE).stream()
                .filter(member -> member.getRole() == RoomMemberRole.OWNER)
                .map(member -> roomRepository.findById(member.getRoomId())
                        .filter(room -> room.getDeletedAt() == null)
                        .map(room -> new WithdrawalCheckResponse.OwnedRoom(room.getId(), room.getName(),
                                roomMemberRepository.countByRoomIdAndStatus(room.getId(), RoomMemberStatus.ACTIVE)))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    private Account require(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND));
    }
}
