package com.fiveoutoffive.moa_backend.room.application.service;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.briefing.application.service.BoardEventRecorder;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventTargetType;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventType;
import com.fiveoutoffive.moa_backend.file.application.service.FileAttachSupport;
import com.fiveoutoffive.moa_backend.file.application.service.FileUploadSupport;
import com.fiveoutoffive.moa_backend.file.domain.model.FileGroup;
import com.fiveoutoffive.moa_backend.file.domain.model.StoredFile;
import com.fiveoutoffive.moa_backend.file.domain.repository.StoredFileRepository;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.type.MemberStatus;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.global.websocket.PresenceReader;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.mood.domain.model.MoodStatus;
import com.fiveoutoffive.moa_backend.mood.domain.repository.MoodStatusRepository;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import com.fiveoutoffive.moa_backend.room.application.command.MemberProfileUpdateCommand;
import com.fiveoutoffive.moa_backend.room.application.command.OwnerDelegationCommand;
import com.fiveoutoffive.moa_backend.room.application.command.RoomCreateCommand;
import com.fiveoutoffive.moa_backend.room.application.command.RoomDeleteCommand;
import com.fiveoutoffive.moa_backend.room.application.command.RoomUpdateCommand;
import com.fiveoutoffive.moa_backend.room.application.result.InviteLinkResponse;
import com.fiveoutoffive.moa_backend.room.application.result.InvitePreviewResponse;
import com.fiveoutoffive.moa_backend.room.application.result.JoinRequestResponse;
import com.fiveoutoffive.moa_backend.room.application.result.LeaveCheckResponse;
import com.fiveoutoffive.moa_backend.room.application.result.MemberProfileResponse;
import com.fiveoutoffive.moa_backend.room.application.result.MemberResponse;
import com.fiveoutoffive.moa_backend.room.application.result.RoomDetailResponse;
import com.fiveoutoffive.moa_backend.room.application.result.RoomSummaryResponse;
import com.fiveoutoffive.moa_backend.room.application.usecase.RoomUseCase;
import com.fiveoutoffive.moa_backend.room.domain.event.RoomCreatedEvent;
import com.fiveoutoffive.moa_backend.room.domain.event.RoomMemberJoinedEvent;
import com.fiveoutoffive.moa_backend.room.domain.event.RoomMemberLeftEvent;
import com.fiveoutoffive.moa_backend.room.domain.model.JoinRequestStatus;
import com.fiveoutoffive.moa_backend.room.domain.model.Room;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomJoinRequest;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMember;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberRole;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberStatus;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomStatus;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomJoinRequestRepository;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomMemberRepository;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomRepository;
import com.fiveoutoffive.moa_backend.room.exception.RoomErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** 방 개설·조회·설정·초대·참여 요청·멤버 관리. */
@Service
@Transactional
@RequiredArgsConstructor
public class RoomService implements RoomUseCase {

    private static final int MAX_MEMBERS = 20;
    private static final int MAX_OWNED_ROOMS = 10;
    private static final int JOIN_REQUEST_EXPIRE_DAYS = 7;

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomJoinRequestRepository joinRequestRepository;
    private final AccountRepository accountRepository;
    private final StoredFileRepository storedFileRepository;
    private final MoodStatusRepository moodStatusRepository;
    private final RoomAccessService access;
    private final FileAttachSupport fileAttachSupport;
    private final FileUploadSupport fileUploadSupport;
    private final NotificationSender notificationSender;
    private final BoardEventRecorder boardEventRecorder;
    private final RealtimePublisher realtime;
    private final PresenceReader presenceReader;
    private final ApplicationEventPublisher eventPublisher;
    private final TaskOpenCountReader taskOpenCountReader;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public RoomDetailResponse create(Long accountId, RoomCreateCommand command, MultipartFile image) {
        if (roomRepository.countByCreatedByAccountIdAndDeletedAtIsNull(accountId) >= MAX_OWNED_ROOMS) {
            throw new BusinessException(RoomErrorCode.ROOM_CREATE_LIMIT_EXCEEDED);
        }
        Room room = roomRepository.save(Room.builder()
                .name(command.name())
                .description(command.description())
                .inviteCode(UUID.randomUUID().toString().replace("-", ""))
                .status(RoomStatus.ACTIVE)
                .createdByAccountId(accountId)
                .layoutVersion(0L)
                .lastActivityAt(KstTime.nowUtc())
                .build());
        roomMemberRepository.save(RoomMember.builder().roomId(room.getId()).accountId(accountId)
                .role(RoomMemberRole.OWNER).status(RoomMemberStatus.ACTIVE).profileSetupAt(KstTime.nowUtc()).build());
        if (image != null && !image.isEmpty()) {
            StoredFile file = fileUploadSupport.upload(room.getId(), accountId, image, FileGroup.ROOM_IMAGE);
            room = roomRepository.save(room.toBuilder().imageFileId(file.getId()).build());
        }
        eventPublisher.publishEvent(new RoomCreatedEvent(room.getId(), accountId));
        return detail(room, accountId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoomSummaryResponse> getMyRooms(Long accountId) {
        Map<Long, RoomMember> mine = roomMemberRepository.findByAccountIdAndStatus(accountId, RoomMemberStatus.ACTIVE).stream()
                .collect(Collectors.toMap(RoomMember::getRoomId, Function.identity()));
        return roomRepository.findByIdInAndDeletedAtIsNull(mine.keySet()).stream()
                .map(room -> new RoomSummaryResponse(room.getId(), room.getName(), imageKey(room), mine.get(room.getId()).getRole(),
                        roomMemberRepository.countByRoomIdAndStatus(room.getId(), RoomMemberStatus.ACTIVE), room.getStatus()))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public RoomDetailResponse getRoom(Long accountId, Long roomId) {
        access.requireMember(roomId, accountId);
        return detail(access.requireRoom(roomId), accountId);
    }

    @Override
    public RoomDetailResponse update(Long accountId, Long roomId, RoomUpdateCommand command) {
        access.requireOwner(roomId, accountId);
        Room room = access.requireRoom(roomId);
        Room.RoomBuilder builder = room.toBuilder();
        if (command.name() != null) {
            builder.name(command.name());
        }
        if (command.description() != null) {
            builder.description(command.description());
        }
        if (command.imageFileId() != null) {
            Long fileId = command.imageFileId().orElse(null);
            if (fileId != null) {
                fileAttachSupport.requireOwnedOfGroup(roomId, accountId, fileId, FileGroup.ROOM_IMAGE);
            }
            builder.imageFileId(fileId);
        }
        Room saved = roomRepository.save(builder.build());
        RoomDetailResponse response = detail(saved, accountId);
        realtime.toRoom(roomId, "ROOM_UPDATED", response, accountId);
        return response;
    }

    @Override
    public void delete(Long accountId, Long roomId, RoomDeleteCommand command) {
        access.requireOwner(roomId, accountId);
        Room room = access.requireRoom(roomId);
        if (!room.getName().equals(command.confirmName())) {
            throw new BusinessException(RoomErrorCode.ROOM_DELETE_CONFIRM_MISMATCH);
        }
        roomRepository.save(room.toBuilder().deletedAt(KstTime.nowUtc()).build());
        realtime.toRoom(roomId, "ROOM_DELETED", Map.of("roomId", roomId), accountId);
    }

    @Transactional(readOnly = true)
    @Override
    public InviteLinkResponse getInviteLink(Long accountId, Long roomId) {
        access.requireOwner(roomId, accountId);
        String code = access.requireRoom(roomId).getInviteCode();
        return new InviteLinkResponse(code, frontendUrl + "/invite/" + code);
    }

    @Transactional(readOnly = true)
    @Override
    public InvitePreviewResponse previewInvite(Long accountId, String inviteCode) {
        Room room = roomByInvite(inviteCode);
        long count = roomMemberRepository.countByRoomIdAndStatus(room.getId(), RoomMemberStatus.ACTIVE);
        return new InvitePreviewResponse(room.getId(), room.getName(), room.getDescription(), imageKey(room), count, inviteStatus(room.getId(), accountId, count));
    }

    @Override
    public JoinRequestResponse requestJoin(Long accountId, String inviteCode) {
        Room room = roomByInvite(inviteCode);
        Optional<RoomMember> existing = roomMemberRepository.findByRoomIdAndAccountId(room.getId(), accountId);
        if (existing.filter(m -> m.getStatus() == RoomMemberStatus.ACTIVE).isPresent()) {
            throw new BusinessException(RoomErrorCode.JOIN_ALREADY_MEMBER, Map.of("roomId", room.getId()));
        }
        if (existing.filter(m -> m.getStatus() == RoomMemberStatus.KICKED).isPresent()) {
            throw new BusinessException(RoomErrorCode.JOIN_KICKED_MEMBER);
        }
        if (joinRequestRepository.findByRoomIdAndAccountIdAndStatus(room.getId(), accountId, JoinRequestStatus.PENDING).isPresent()) {
            throw new BusinessException(RoomErrorCode.JOIN_REQUEST_ALREADY_PENDING);
        }
        if (roomMemberRepository.countByRoomIdAndStatus(room.getId(), RoomMemberStatus.ACTIVE) >= MAX_MEMBERS) {
            throw new BusinessException(RoomErrorCode.ROOM_MEMBER_LIMIT_EXCEEDED);
        }
        LocalDateTime now = KstTime.nowUtc();
        RoomJoinRequest saved = joinRequestRepository.save(RoomJoinRequest.builder().roomId(room.getId()).accountId(accountId)
                .status(JoinRequestStatus.PENDING).requestedAt(now).expiresAt(now.plusDays(JOIN_REQUEST_EXPIRE_DAYS)).build());
        String requesterName = accountRepository.findById(accountId).map(Account::getName).orElse("");
        access.ownerId(room.getId()).ifPresent(ownerId -> notificationSender.send(room.getId(), accountId, NotificationType.JOIN_REQUESTED,
                List.of(ownerId), "참여 요청이 접수되었습니다", requesterName + "님이 참여를 요청했습니다", "JOIN_REQUEST", saved.getId()));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<JoinRequestResponse> getJoinRequests(Long accountId, Long roomId, JoinRequestStatus status, Pageable pageable) {
        access.requireOwner(roomId, accountId);
        return Pages.response(joinRequestRepository.findByRoomIdAndStatus(roomId, status, pageable).map(this::toResponse));
    }

    @Override
    public JoinRequestResponse approve(Long accountId, Long roomId, Long requestId) {
        access.requireOwner(roomId, accountId);
        // 동시 승인으로 20명을 넘지 않도록 방 행을 잠근 뒤 인원을 센다.
        roomRepository.findLockedById(roomId).orElseThrow(() -> new BusinessException(RoomErrorCode.ROOM_NOT_FOUND));
        RoomJoinRequest request = pendingRequest(roomId, requestId);
        if (roomMemberRepository.countByRoomIdAndStatus(roomId, RoomMemberStatus.ACTIVE) >= MAX_MEMBERS) {
            throw new BusinessException(RoomErrorCode.ROOM_MEMBER_LIMIT_EXCEEDED);
        }
        LocalDateTime now = KstTime.nowUtc();
        RoomJoinRequest saved = joinRequestRepository.save(request.toBuilder().status(JoinRequestStatus.APPROVED).processedAt(now).processedByAccountId(accountId).build());
        Long joinerId = request.getAccountId();
        RoomMember member = roomMemberRepository.findByRoomIdAndAccountId(roomId, joinerId)
                .map(left -> left.toBuilder().status(RoomMemberStatus.ACTIVE).role(RoomMemberRole.MEMBER).joinedAt(now).leftAt(null).profileSetupAt(null).build())
                .orElseGet(() -> RoomMember.builder().roomId(roomId).accountId(joinerId).role(RoomMemberRole.MEMBER).status(RoomMemberStatus.ACTIVE).joinedAt(now).lastBriefingCheckedAt(now).build());
        roomMemberRepository.save(member);
        eventPublisher.publishEvent(new RoomMemberJoinedEvent(roomId, joinerId));
        boardEventRecorder.record(roomId, BoardEventType.MEMBER_JOINED, joinerId, null, BoardEventTargetType.MEMBER, joinerId, null);
        notificationSender.send(roomId, accountId, NotificationType.JOIN_PROCESSED, List.of(joinerId), "참여 요청이 승인되었습니다", access.roomName(roomId), "ROOM", roomId);
        notificationSender.send(roomId, joinerId, NotificationType.MEMBER_JOINED, access.activeMemberIds(roomId), "새 멤버가 합류했습니다", null, "MEMBER", joinerId);
        realtime.toRoom(roomId, "MEMBER_JOINED", access.memberRef(roomId, joinerId), joinerId);
        return toResponse(saved);
    }

    @Override
    public JoinRequestResponse reject(Long accountId, Long roomId, Long requestId) {
        access.requireOwner(roomId, accountId);
        RoomJoinRequest request = pendingRequest(roomId, requestId);
        RoomJoinRequest saved = joinRequestRepository.save(request.toBuilder().status(JoinRequestStatus.REJECTED).processedAt(KstTime.nowUtc()).processedByAccountId(accountId).build());
        notificationSender.send(roomId, accountId, NotificationType.JOIN_PROCESSED, List.of(request.getAccountId()), "참여 요청이 거절되었습니다", access.roomName(roomId), "ROOM", roomId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MemberResponse> getMembers(Long accountId, Long roomId) {
        access.requireMember(roomId, accountId);
        List<RoomMember> members = access.activeMembers(roomId);
        List<Long> ids = members.stream().map(RoomMember::getAccountId).toList();
        Map<Long, MemberRef> refs = access.memberRefs(roomId, ids);
        Set<Long> online = presenceReader.onlineOf(ids);
        Map<Long, MoodStatus> moods = moodStatusRepository.findByRoomIdAndRecordedDate(roomId, KstTime.todayKst()).stream()
                .collect(Collectors.toMap(MoodStatus::getAccountId, Function.identity()));
        return members.stream().map(member -> {
            MemberRef ref = refs.get(member.getAccountId());
            MoodStatus mood = moods.get(member.getAccountId());
            return new MemberResponse(member.getAccountId(), ref.displayName(), ref.profileImageUrl(), member.getPositionLabel(), member.getRole(),
                    online.contains(member.getAccountId()), mood == null ? null : new MemberResponse.TodayMood(mood.getMood(), mood.getComment()));
        }).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public MemberProfileResponse getMemberProfile(Long accountId, Long roomId, Long targetId) {
        access.requireMember(roomId, accountId);
        return profile(roomId, targetId, accountId);
    }

    @Override
    public MemberProfileResponse updateMyProfile(Long accountId, Long roomId, MemberProfileUpdateCommand command) {
        RoomMember member = access.requireMember(roomId, accountId);
        String nickname = command.nickname();
        if (nickname != null && !nickname.equals(member.getNickname())
                && roomMemberRepository.existsByRoomIdAndNicknameAndStatus(roomId, nickname, RoomMemberStatus.ACTIVE)) {
            throw new BusinessException(RoomErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }
        if (command.profileFileId() != null) {
            fileAttachSupport.requireOwnedOfGroup(roomId, accountId, command.profileFileId(), FileGroup.PROFILE_IMAGE);
        }
        roomMemberRepository.save(member.toBuilder().nickname(nickname).profileFileId(command.profileFileId())
                .positionLabel(command.positionLabel()).profileSetupAt(KstTime.nowUtc()).build());
        realtime.toRoom(roomId, "MEMBER_UPDATED", access.memberRef(roomId, accountId), accountId);
        return profile(roomId, accountId, accountId);
    }

    @Transactional(readOnly = true)
    @Override
    public LeaveCheckResponse checkLeave(Long accountId, Long roomId) {
        RoomMember member = access.requireMember(roomId, accountId);
        boolean owner = member.getRole() == RoomMemberRole.OWNER;
        return new LeaveCheckResponse(!owner, owner, taskOpenCountReader.countAssignedOpen(roomId, accountId));
    }

    @Override
    public void leave(Long accountId, Long roomId) {
        RoomMember member = access.requireMember(roomId, accountId);
        if (member.getRole() == RoomMemberRole.OWNER) {
            throw new BusinessException(RoomErrorCode.MEMBER_OWNER_CANNOT_LEAVE);
        }
        roomMemberRepository.save(member.toBuilder().status(RoomMemberStatus.LEFT).nickname(null).profileFileId(null).leftAt(KstTime.nowUtc()).build());
        eventPublisher.publishEvent(new RoomMemberLeftEvent(roomId, accountId, RoomMemberStatus.LEFT));
        realtime.toRoom(roomId, "MEMBER_LEFT", Map.of("accountId", accountId), accountId);
    }

    @Override
    public void kick(Long accountId, Long roomId, Long targetId) {
        access.requireOwner(roomId, accountId);
        if (accountId.equals(targetId)) {
            throw new BusinessException(RoomErrorCode.MEMBER_CANNOT_KICK_SELF);
        }
        RoomMember target = activeMember(roomId, targetId);
        roomMemberRepository.save(target.toBuilder().status(RoomMemberStatus.KICKED).nickname(null).profileFileId(null).kickedAt(KstTime.nowUtc()).build());
        eventPublisher.publishEvent(new RoomMemberLeftEvent(roomId, targetId, RoomMemberStatus.KICKED));
        notificationSender.send(roomId, accountId, NotificationType.MEMBER_KICKED, List.of(targetId), "방에서 내보내졌습니다", access.roomName(roomId), "ROOM", roomId);
        realtime.toRoom(roomId, "MEMBER_LEFT", Map.of("accountId", targetId), accountId);
        // 이미 맺은 구독을 끊는다. 재연결하면 이 방은 구독 권한 검사에서 거절된다.
        realtime.disconnectUser(String.valueOf(targetId));
    }

    @Override
    public List<MemberResponse> delegateOwner(Long accountId, Long roomId, OwnerDelegationCommand command) {
        RoomMember owner = access.requireOwner(roomId, accountId);
        if (accountId.equals(command.targetAccountId())) {
            throw new BusinessException(RoomErrorCode.MEMBER_DELEGATE_TARGET_INVALID);
        }
        RoomMember target = activeMember(roomId, command.targetAccountId());
        // 방장 1명 유니크 인덱스 때문에 내리는 쪽을 먼저 반영한다.
        roomMemberRepository.saveAndFlush(owner.toBuilder().role(RoomMemberRole.MEMBER).build());
        roomMemberRepository.save(target.toBuilder().role(RoomMemberRole.OWNER).build());
        notificationSender.send(roomId, accountId, NotificationType.ROLE_CHANGED, List.of(target.getAccountId()), "방장 권한을 위임받았습니다", access.roomName(roomId), "ROOM", roomId);
        realtime.toRoom(roomId, "MEMBER_UPDATED", access.memberRef(roomId, target.getAccountId()), accountId);
        return getMembers(accountId, roomId);
    }

    private RoomDetailResponse detail(Room room, Long accountId) {
        RoomMember me = roomMemberRepository.findByRoomIdAndAccountId(room.getId(), accountId).orElseThrow(() -> new BusinessException(RoomErrorCode.ROOM_NOT_MEMBER));
        MemberRef ref = access.memberRef(room.getId(), accountId);
        return new RoomDetailResponse(room.getId(), room.getName(), room.getDescription(), imageKey(room), room.getStatus(),
                roomMemberRepository.countByRoomIdAndStatus(room.getId(), RoomMemberStatus.ACTIVE), MAX_MEMBERS,
                new RoomDetailResponse.MyMembership(me.getRole(), me.getNickname(), ref.displayName(), ref.profileImageUrl(), me.getPositionLabel(), me.getNotificationMuted(), me.getJoinedAt()),
                me.getProfileSetupAt() == null);
    }

    private MemberProfileResponse profile(Long roomId, Long targetId, Long viewerId) {
        RoomMember target = roomMemberRepository.findByRoomIdAndAccountId(roomId, targetId).orElseThrow(() -> new BusinessException(RoomErrorCode.MEMBER_NOT_FOUND));
        MemberRef ref = access.memberRef(roomId, targetId);
        MoodStatus mood = moodStatusRepository.findFirstByRoomIdAndAccountIdOrderByRecordedDateDesc(roomId, targetId).orElse(null);
        boolean active = ref.memberStatus() == MemberStatus.ACTIVE;
        return new MemberProfileResponse(targetId, ref.memberStatus(), ref.displayName(), ref.profileImageUrl(), active ? target.getPositionLabel() : null, target.getRole(),
                active && presenceReader.onlineOf(List.of(targetId)).contains(targetId),
                mood == null || !active ? null : new MemberProfileResponse.LatestMood(mood.getMood(), mood.getComment()),
                active && !targetId.equals(viewerId));
    }

    private RoomMember activeMember(Long roomId, Long accountId) {
        return roomMemberRepository.findByRoomIdAndAccountId(roomId, accountId)
                .filter(m -> m.getStatus() == RoomMemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(RoomErrorCode.MEMBER_NOT_FOUND));
    }

    private Room roomByInvite(String inviteCode) {
        Room room = roomRepository.findByInviteCodeAndDeletedAtIsNull(inviteCode).orElseThrow(() -> new BusinessException(RoomErrorCode.INVITE_CODE_NOT_FOUND));
        if (room.getStatus() == RoomStatus.INACTIVE) {
            throw new BusinessException(RoomErrorCode.ROOM_INACTIVE);
        }
        return room;
    }

    private String inviteStatus(Long roomId, Long accountId, long count) {
        Optional<RoomMember> member = roomMemberRepository.findByRoomIdAndAccountId(roomId, accountId);
        if (member.filter(m -> m.getStatus() == RoomMemberStatus.ACTIVE).isPresent()) {
            return "MEMBER";
        }
        if (member.filter(m -> m.getStatus() == RoomMemberStatus.KICKED).isPresent()) {
            return "KICKED";
        }
        if (joinRequestRepository.findByRoomIdAndAccountIdAndStatus(roomId, accountId, JoinRequestStatus.PENDING).isPresent()) {
            return "PENDING";
        }
        return count >= MAX_MEMBERS ? "FULL" : "NONE";
    }

    private RoomJoinRequest pendingRequest(Long roomId, Long requestId) {
        RoomJoinRequest request = joinRequestRepository.findById(requestId)
                .filter(found -> found.getRoomId().equals(roomId))
                .orElseThrow(() -> new BusinessException(RoomErrorCode.JOIN_REQUEST_NOT_FOUND));
        if (request.getStatus() != JoinRequestStatus.PENDING) {
            throw new BusinessException(RoomErrorCode.JOIN_REQUEST_ALREADY_PROCESSED);
        }
        return request;
    }

    private JoinRequestResponse toResponse(RoomJoinRequest r) {
        String name = accountRepository.findById(r.getAccountId()).map(Account::getName).orElse(null);
        return new JoinRequestResponse(r.getId(), r.getRoomId(), new JoinRequestResponse.Requester(r.getAccountId(), name), r.getStatus(), r.getRequestedAt(), r.getExpiresAt(), r.getProcessedAt());
    }

    private String imageKey(Room room) {
        return room.getImageFileId() == null ? null : storedFileRepository.findById(room.getImageFileId()).map(StoredFile::getObjectKey).orElse(null);
    }
}
