package com.fiveoutoffive.moa_backend.notice.application.service;

import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.notice.domain.model.Notice;
import com.fiveoutoffive.moa_backend.notice.domain.repository.NoticeRepository;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 노출이 시작된 공지를 전체 사용자에게 알린다. 예약 공지도 시작 시각에 맞춰 보낸다. */
@Component
@RequiredArgsConstructor
public class NoticeBroadcaster {

    private static final long CHECK_MILLIS = 60_000;

    private final NoticeRepository noticeRepository;
    private final AccountRepository accountRepository;
    private final NotificationSender notificationSender;

    public void broadcast(Notice notice) {
        notificationSender.send(null, null, NotificationType.NOTICE, accountRepository.findActiveIds(), notice.getTitle(), null, "NOTICE", notice.getId());
    }

    @Scheduled(fixedRate = CHECK_MILLIS)
    @Transactional
    public void broadcastStarted() {
        LocalDateTime now = KstTime.nowUtc();
        noticeRepository.findStartingBetween(now.minus(Duration.ofMillis(CHECK_MILLIS)), now).forEach(this::broadcast);
    }
}
