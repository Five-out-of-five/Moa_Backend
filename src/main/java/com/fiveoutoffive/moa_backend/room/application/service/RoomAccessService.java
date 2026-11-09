package com.fiveoutoffive.moa_backend.room.application.service;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountStatus;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.file.domain.model.StoredFile;
import com.fiveoutoffive.moa_backend.file.domain.repository.StoredFileRepository;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.type.MemberStatus;
import com.fiveoutoffive.moa_backend.room.domain.model.Room;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMember;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberRole;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberStatus;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomStatus;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomMemberRepository;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomRepository;
import com.fiveoutoffive.moa_backend.room.exception.RoomErrorCode;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 다른 도메인이 쓰는 방 권한 확인과 표시 이름 조회. 방 API 공통 에러를 여기서 낸다. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomAccessService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final AccountRepository accountRepository;
    private final StoredFileRepository storedFileRepository;

    public Room requireRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .filter(found -> found.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(RoomErrorCode.ROOM_NOT_FOUND));
        if (room.getStatus() == RoomStatus.INACTIVE) {
            throw new BusinessException(RoomErrorCode.ROOM_INACTIVE);
        }
        return room;
    }

    public RoomMember requireMember(Long roomId, Long accountId) {
        requireRoom(roomId);
        return roomMemberRepository.findByRoomIdAndAccountId(roomId, accountId)
                .filter(member -> member.getStatus() == RoomMemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(RoomErrorCode.ROOM_NOT_MEMBER));
    }

    public RoomMember requireOwner(Long roomId, Long accountId) {
        RoomMember member = requireMember(roomId, accountId);
        if (member.getRole() != RoomMemberRole.OWNER) {
            throw new BusinessException(RoomErrorCode.ROOM_OWNER_ONLY);
        }
        return member;
    }

    public boolean isActiveMember(Long roomId, Long accountId) {
        return roomMemberRepository.findByRoomIdAndAccountId(roomId, accountId)
                .filter(member -> member.getStatus() == RoomMemberStatus.ACTIVE)
                .isPresent();
    }

    public boolean isOwner(Long roomId, Long accountId) {
        return roomMemberRepository.findByRoomIdAndAccountId(roomId, accountId)
                .filter(member -> member.getStatus() == RoomMemberStatus.ACTIVE && member.getRole() == RoomMemberRole.OWNER)
                .isPresent();
    }

    public List<RoomMember> activeMembers(Long roomId) {
        return roomMemberRepository.findByRoomIdAndStatus(roomId, RoomMemberStatus.ACTIVE);
    }

    public List<Long> activeMemberIds(Long roomId) {
        return activeMembers(roomId).stream().map(RoomMember::getAccountId).toList();
    }

    public Optional<Long> ownerId(Long roomId) {
        return activeMembers(roomId).stream().filter(m -> m.getRole() == RoomMemberRole.OWNER).map(RoomMember::getAccountId).findFirst();
    }

    public String roomName(Long roomId) {
        return roomRepository.findById(roomId).map(Room::getName).orElse(null);
    }

    public MemberRef memberRef(Long roomId, Long accountId) {
        return accountId == null ? null : memberRefs(roomId, Set.of(accountId)).get(accountId);
    }

    /** 공통 규칙 "표시 이름 (MemberRef)". 방 행이 없는 계정(나간 뒤 파기 등)도 빠짐없이 돌려준다. */
    public Map<Long, MemberRef> memberRefs(Long roomId, Collection<Long> accountIds) {
        Set<Long> ids = accountIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, RoomMember> members = roomMemberRepository.findByRoomIdAndAccountIdIn(roomId, ids).stream()
                .collect(Collectors.toMap(RoomMember::getAccountId, Function.identity()));
        Map<Long, Account> accounts = accountRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));
        Set<Long> fileIds = members.values().stream().map(RoomMember::getProfileFileId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> keys = storedFileRepository.findByIdIn(fileIds).stream()
                .collect(Collectors.toMap(StoredFile::getId, StoredFile::getObjectKey));
        Map<Long, MemberRef> result = new HashMap<>();
        for (Long id : ids) {
            RoomMember member = members.get(id);
            Account account = accounts.get(id);
            MemberStatus status = statusOf(member, account);
            String name = member != null && member.getNickname() != null ? member.getNickname() : account != null ? account.getName() : null;
            String key = member == null ? null : keys.get(member.getProfileFileId());
            result.put(id, MemberRef.of(id, name, key, status));
        }
        return result;
    }

    private MemberStatus statusOf(RoomMember member, Account account) {
        if (account == null || account.getStatus() == AccountStatus.WITHDRAWN) {
            return MemberStatus.WITHDRAWN;
        }
        if (member == null) {
            return MemberStatus.LEFT;
        }
        return MemberStatus.valueOf(member.getStatus().name());
    }
}
