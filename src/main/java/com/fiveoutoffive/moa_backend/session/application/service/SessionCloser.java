package com.fiveoutoffive.moa_backend.session.application.service;

import com.fiveoutoffive.moa_backend.briefing.application.service.BoardEventRecorder;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventTargetType;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventType;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import com.fiveoutoffive.moa_backend.session.domain.event.SessionClosedEvent;
import com.fiveoutoffive.moa_backend.session.domain.model.BoardSession;
import com.fiveoutoffive.moa_backend.session.domain.model.SessionStatus;
import com.fiveoutoffive.moa_backend.session.domain.repository.BoardSessionRepository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** 수동 마감과 자동 마감이 같은 처리를 하도록 한 곳에 모았다. */
@Component
@RequiredArgsConstructor
public class SessionCloser {

    private final BoardSessionRepository repository;
    private final RoomAccessService access;
    private final BoardEventRecorder eventRecorder;
    private final NotificationSender notificationSender;
    private final RealtimePublisher realtime;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 진행 중일 때만 마감하고 후속 처리(브리핑·알림·실시간·마감 이벤트)를 한 번 한다.
     * 두 멤버가 동시에 마감하거나 수동·자동 마감이 겹치면 늦은 쪽은 빈 값이다(후속 처리가 두 번 돌지 않게).
     */
    public Optional<BoardSession> close(BoardSession session, Long closedBy) {
        LocalDateTime closedAt = KstTime.nowUtc();
        if (!repository.closeIfInProgress(session.getId(), closedAt, closedBy)) {
            return Optional.empty();
        }
        BoardSession saved = session.toBuilder().status(SessionStatus.CLOSED).closedAt(closedAt).closedByAccountId(closedBy).build();
        Long roomId = saved.getRoomId();
        eventRecorder.record(roomId, BoardEventType.SESSION_CLOSED, closedBy, null, BoardEventTargetType.SESSION, saved.getId(), saved.getTitle());
        notificationSender.send(roomId, closedBy, NotificationType.SESSION_CLOSED, access.activeMemberIds(roomId), "회차가 마감되었습니다", saved.getTitle(), "SESSION", saved.getId());
        realtime.toRoom(roomId, "SESSION_CLOSED", Map.of("sessionId", saved.getId()), closedBy);
        eventPublisher.publishEvent(new SessionClosedEvent(roomId, saved.getId(), saved.getTitle()));
        return Optional.of(saved);
    }
}
