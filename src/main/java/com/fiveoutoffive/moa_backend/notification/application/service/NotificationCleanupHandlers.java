package com.fiveoutoffive.moa_backend.notification.application.service;

import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.notification.domain.repository.NotificationRepository;
import com.fiveoutoffive.moa_backend.notification.domain.repository.NotificationSettingRepository;
import com.fiveoutoffive.moa_backend.room.domain.event.RoomMemberLeftEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 90일 경과 알림 삭제. 방을 나가면 그 방의 알림 설정과 알림을 지운다. (요구사항 62·65) */
@Component
@RequiredArgsConstructor
public class NotificationCleanupHandlers {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository settingRepository;

    @Scheduled(cron = "0 30 19 * * *", zone = "UTC")
    @Transactional
    public void deleteOld() {
        notificationRepository.deleteOlderThan(KstTime.nowUtc().minusDays(90));
    }

    @EventListener
    public void onMemberLeft(RoomMemberLeftEvent event) {
        settingRepository.deleteByAccountIdAndRoomId(event.accountId(), event.roomId());
        notificationRepository.deleteLeftRoomNotifications(event.accountId(), event.roomId());
    }
}
