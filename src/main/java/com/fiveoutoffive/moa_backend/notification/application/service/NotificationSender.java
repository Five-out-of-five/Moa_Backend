package com.fiveoutoffive.moa_backend.notification.application.service;

import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.notification.application.result.NotificationEvent;
import com.fiveoutoffive.moa_backend.notification.application.result.NotificationResponse;
import com.fiveoutoffive.moa_backend.notification.domain.model.Notification;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationGroup;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationSetting;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import com.fiveoutoffive.moa_backend.notification.domain.repository.NotificationRepository;
import com.fiveoutoffive.moa_backend.notification.domain.repository.NotificationSettingRepository;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMember;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberStatus;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomMemberRepository;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomRepository;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 모든 도메인이 쓰는 알림 생성 창구. 수신 판단: 방 전체 끄기 > 방별 그룹 > 전역 그룹 > 기본 ON (SYSTEM 은 항상 받음).
 *
 * <p>방 알림은 그 방의 활성 멤버에게만 만든다(나간 방의 알림은 보이지 않아야 한다). 단, 방을 나간 사람에게 가는
 * 퇴출·참여 결과 알림은 예외로 전역 설정만 본다.
 */
@Component
@RequiredArgsConstructor
public class NotificationSender {

    private static final Map<NotificationType, NotificationGroup> GROUPS = new java.util.EnumMap<>(NotificationType.class);

    /** 방 멤버가 아닌 사람에게도 가는 알림 (퇴출 통보, 참여 요청 승인·거절 결과) */
    private static final Set<NotificationType> NON_MEMBER_TYPES = EnumSet.of(NotificationType.MEMBER_KICKED, NotificationType.JOIN_PROCESSED);

    static {
        put(NotificationGroup.MY_ACTIVITY, NotificationType.CARD_COMMENTED, NotificationType.CARD_REACTED, NotificationType.CARD_CONVERTED);
        put(NotificationGroup.TASK, NotificationType.TASK_ASSIGNED, NotificationType.TASK_DUE_SOON, NotificationType.TASK_STATUS_CHANGED, NotificationType.TASK_DELETED, NotificationType.TASK_REJECTED, NotificationType.TASK_UNASSIGNED);
        put(NotificationGroup.VOTE, NotificationType.VOTE_CREATED, NotificationType.VOTE_CLOSED, NotificationType.VOTE_DELETED, NotificationType.VOTE_REMINDER, NotificationType.VOTE_DEADLINE_CHANGED);
        put(NotificationGroup.SESSION, NotificationType.SESSION_STARTED, NotificationType.SESSION_CLOSED);
        put(NotificationGroup.MEMBER_ROLE, NotificationType.JOIN_REQUESTED, NotificationType.JOIN_PROCESSED, NotificationType.CHANNEL_INVITED, NotificationType.MEMBER_JOINED, NotificationType.ROLE_CHANGED, NotificationType.MEMBER_KICKED);
        put(NotificationGroup.SYSTEM, NotificationType.ACCOUNT_SUSPENDED, NotificationType.ROOM_INACTIVATED, NotificationType.REPORT_RECEIVED, NotificationType.REPORT_PROCESSED, NotificationType.NOTICE);
    }

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository settingRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomRepository roomRepository;
    private final RealtimePublisher realtime;

    private static void put(NotificationGroup group, NotificationType... types) {
        for (NotificationType type : types) {
            GROUPS.put(type, group);
        }
    }

    /** @param actorId 이벤트를 일으킨 계정. 본인에게는 알림을 만들지 않는다. 시스템이 일으켰으면 null. */
    public void send(Long roomId, Long actorId, NotificationType type, Collection<Long> recipients, String title, String content, String targetType, Long targetId) {
        NotificationGroup group = GROUPS.get(type);
        for (Long recipient : recipients) {
            if (recipient == null || recipient.equals(actorId) || !accepts(recipient, roomId, type, group)) {
                continue;
            }
            Notification saved = notificationRepository.save(Notification.builder().ownerAccountId(recipient).roomId(roomId)
                    .notificationGroup(group).notificationType(type).title(title).content(content).targetType(targetType).targetId(targetId).read(false).build());
            realtime.toUser(recipient, "notifications", new NotificationEvent(toResponse(saved),
                    notificationRepository.countByOwnerAccountIdAndRead(recipient, false)));
        }
    }

    public NotificationResponse toResponse(Notification n) {
        NotificationResponse.RoomInfo room = n.getRoomId() == null ? null : roomRepository.findById(n.getRoomId())
                .map(found -> new NotificationResponse.RoomInfo(found.getId(), found.getName())).orElse(null);
        return new NotificationResponse(n.getId(), n.getNotificationGroup(), n.getNotificationType(), n.getTitle(), n.getContent(), room, n.getTargetType(), n.getTargetId(), n.getRead(), n.getCreatedAt());
    }

    private boolean accepts(Long accountId, Long roomId, NotificationType type, NotificationGroup group) {
        if (group == NotificationGroup.SYSTEM) {
            return true;
        }
        if (roomId != null && !NON_MEMBER_TYPES.contains(type)) {
            Optional<RoomMember> member = roomMemberRepository.findByRoomIdAndAccountId(roomId, accountId);
            if (member.isEmpty() || member.get().getStatus() != RoomMemberStatus.ACTIVE) {
                return false;
            }
            if (Boolean.TRUE.equals(member.get().getNotificationMuted())) {
                return false;
            }
            Optional<Boolean> roomSetting = find(settingRepository.findByAccountIdAndRoomId(accountId, roomId), group);
            if (roomSetting.isPresent()) {
                return roomSetting.get();
            }
        }
        return find(settingRepository.findByAccountIdAndRoomIdIsNull(accountId), group).orElse(true);
    }

    private Optional<Boolean> find(List<NotificationSetting> settings, NotificationGroup group) {
        return settings.stream().filter(s -> s.getNotificationGroup().name().equals(group.name())).map(NotificationSetting::getEnabled).findFirst();
    }
}
