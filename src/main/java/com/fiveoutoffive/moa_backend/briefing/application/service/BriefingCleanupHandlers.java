package com.fiveoutoffive.moa_backend.briefing.application.service;

import com.fiveoutoffive.moa_backend.briefing.domain.repository.BoardEventRepository;
import com.fiveoutoffive.moa_backend.briefing.domain.repository.BriefingDismissalRepository;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.room.domain.event.RoomMemberLeftEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BriefingCleanupHandlers {

    private final BoardEventRepository eventRepository;
    private final BriefingDismissalRepository dismissalRepository;

    @Scheduled(cron = "0 40 19 * * *", zone = "UTC")
    @Transactional
    public void deleteOld() {
        eventRepository.deleteOlderThan(KstTime.nowUtc().minusDays(30));
    }

    @EventListener
    public void onMemberLeft(RoomMemberLeftEvent event) {
        dismissalRepository.deleteByRoomIdAndAccountId(event.roomId(), event.accountId());
    }
}
