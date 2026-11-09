package com.fiveoutoffive.moa_backend.notification.application.service;

import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.notification.application.command.GroupSettingCommand;
import com.fiveoutoffive.moa_backend.notification.application.result.NotificationResponse;
import com.fiveoutoffive.moa_backend.notification.application.result.NotificationSettingResponse;
import com.fiveoutoffive.moa_backend.notification.application.result.RoomNotificationSettingResponse;
import com.fiveoutoffive.moa_backend.notification.application.result.UnreadCountResponse;
import com.fiveoutoffive.moa_backend.notification.application.usecase.NotificationUseCase;
import com.fiveoutoffive.moa_backend.notification.domain.model.Notification;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationGroup;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationSetting;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationSettingGroup;
import com.fiveoutoffive.moa_backend.notification.domain.repository.NotificationRepository;
import com.fiveoutoffive.moa_backend.notification.domain.repository.NotificationSettingRepository;
import com.fiveoutoffive.moa_backend.notification.exception.NotificationErrorCode;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMember;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomMemberRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService implements NotificationUseCase {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository settingRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomAccessService access;
    private final NotificationSender sender;

    @Transactional(readOnly = true)
    @Override
    public PageResponse<NotificationResponse> getList(Long accountId, boolean unreadOnly, Pageable pageable) {
        Page<Notification> page = unreadOnly ? notificationRepository.findByOwnerAccountIdAndRead(accountId, false, pageable) : notificationRepository.findByOwnerAccountId(accountId, pageable);
        return Pages.response(page.map(sender::toResponse));
    }

    @Transactional(readOnly = true)
    @Override
    public UnreadCountResponse getUnreadCount(Long accountId) {
        return new UnreadCountResponse(notificationRepository.countByOwnerAccountIdAndRead(accountId, false));
    }

    @Override
    public void markRead(Long accountId, Long notificationId) {
        notificationRepository.save(mine(accountId, notificationId).toBuilder().read(true).readAt(KstTime.nowUtc()).build());
    }

    @Override
    public void markAllRead(Long accountId) {
        notificationRepository.markAllRead(accountId, KstTime.nowUtc());
    }

    @Override
    public void delete(Long accountId, Long notificationId) {
        notificationRepository.deleteById(mine(accountId, notificationId).getId());
    }

    @Transactional(readOnly = true)
    @Override
    public NotificationSettingResponse getGlobalSettings(Long accountId) {
        Map<String, Boolean> saved = toMap(settingRepository.findByAccountIdAndRoomIdIsNull(accountId));
        return new NotificationSettingResponse(Arrays.stream(NotificationGroup.values()).map(group -> {
            boolean locked = group == NotificationGroup.SYSTEM;
            return new NotificationSettingResponse.GroupSetting(group, locked || saved.getOrDefault(group.name(), true), locked);
        }).toList());
    }

    @Override
    public NotificationSettingResponse updateGlobalSettings(Long accountId, List<GroupSettingCommand> groups) {
        upsert(accountId, null, groups);
        return getGlobalSettings(accountId);
    }

    @Transactional(readOnly = true)
    @Override
    public RoomNotificationSettingResponse getRoomSettings(Long accountId, Long roomId) {
        RoomMember member = access.requireMember(roomId, accountId);
        Map<String, Boolean> global = toMap(settingRepository.findByAccountIdAndRoomIdIsNull(accountId));
        Map<String, Boolean> room = toMap(settingRepository.findByAccountIdAndRoomId(accountId, roomId));
        boolean muted = Boolean.TRUE.equals(member.getNotificationMuted());
        return new RoomNotificationSettingResponse(muted, Arrays.stream(NotificationGroup.values()).map(group -> {
            Boolean enabled = room.get(group.name());
            boolean effective = group == NotificationGroup.SYSTEM || (!muted && (enabled != null ? enabled : global.getOrDefault(group.name(), true)));
            return new RoomNotificationSettingResponse.GroupSetting(group, enabled, effective);
        }).toList());
    }

    @Override
    public RoomNotificationSettingResponse updateRoomSettings(Long accountId, Long roomId, boolean muted, List<GroupSettingCommand> groups) {
        RoomMember member = access.requireMember(roomId, accountId);
        roomMemberRepository.save(member.toBuilder().notificationMuted(muted).build());
        if (groups != null) {
            upsert(accountId, roomId, groups);
        }
        return getRoomSettings(accountId, roomId);
    }

    private void upsert(Long accountId, Long roomId, List<GroupSettingCommand> groups) {
        if (groups.stream().anyMatch(g -> g.group() == NotificationGroup.SYSTEM)) {
            throw new BusinessException(NotificationErrorCode.NOTIFICATION_GROUP_LOCKED);
        }
        Map<String, NotificationSetting> existing = (roomId == null ? settingRepository.findByAccountIdAndRoomIdIsNull(accountId) : settingRepository.findByAccountIdAndRoomId(accountId, roomId))
                .stream().collect(Collectors.toMap(s -> s.getNotificationGroup().name(), Function.identity()));
        for (GroupSettingCommand group : groups) {
            NotificationSettingGroup target = NotificationSettingGroup.valueOf(group.group().name());
            NotificationSetting current = existing.get(target.name());
            if (group.enabled() == null) {
                if (current != null) {
                    settingRepository.deleteSetting(current.getId());
                }
                continue;
            }
            settingRepository.save((current != null ? current.toBuilder() : NotificationSetting.builder().accountId(accountId).roomId(roomId).notificationGroup(target)).enabled(group.enabled()).build());
        }
    }

    private Map<String, Boolean> toMap(List<NotificationSetting> settings) {
        return settings.stream().collect(Collectors.toMap(s -> s.getNotificationGroup().name(), NotificationSetting::getEnabled));
    }

    private Notification mine(Long accountId, Long notificationId) {
        return notificationRepository.findById(notificationId)
                .filter(n -> n.getOwnerAccountId().equals(accountId))
                .orElseThrow(() -> new BusinessException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
    }
}
