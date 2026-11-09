package com.fiveoutoffive.moa_backend.session.application.service;

import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.IdeaCardRepository;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import com.fiveoutoffive.moa_backend.session.application.command.SessionStartCommand;
import com.fiveoutoffive.moa_backend.session.application.result.SessionResponse;
import com.fiveoutoffive.moa_backend.session.application.result.SessionSummaryResponse;
import com.fiveoutoffive.moa_backend.session.application.usecase.SessionUseCase;
import com.fiveoutoffive.moa_backend.session.domain.model.BoardSession;
import com.fiveoutoffive.moa_backend.session.domain.model.SessionStatus;
import com.fiveoutoffive.moa_backend.session.domain.repository.BoardSessionRepository;
import com.fiveoutoffive.moa_backend.session.exception.SessionErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SessionService implements SessionUseCase {

    private final BoardSessionRepository repository;
    private final IdeaCardRepository cardRepository;
    private final SessionCloser closer;
    private final RoomAccessService access;
    private final NotificationSender notificationSender;
    private final RealtimePublisher realtime;

    @Override
    public SessionResponse start(Long accountId, Long roomId, SessionStartCommand command) {
        access.requireMember(roomId, accountId);
        if (repository.findFirstByRoomIdAndStatus(roomId, SessionStatus.IN_PROGRESS).isPresent()) {
            throw new BusinessException(SessionErrorCode.SESSION_ALREADY_IN_PROGRESS);
        }
        LocalDateTime deadline = KstTime.toUtc(command.deadlineAt());
        if (deadline != null && deadline.isBefore(KstTime.nowUtc())) {
            throw new BusinessException(SessionErrorCode.SESSION_DEADLINE_IN_PAST);
        }
        BoardSession saved = repository.save(BoardSession.builder().roomId(roomId).sessionNo(repository.findMaxSessionNo(roomId) + 1)
                .title(command.title()).status(SessionStatus.IN_PROGRESS).deadlineAt(deadline).startedByAccountId(accountId).startedAt(KstTime.nowUtc()).build());
        SessionResponse response = toResponse(saved, false);
        notificationSender.send(roomId, accountId, NotificationType.SESSION_STARTED, access.activeMemberIds(roomId), "새 회차가 시작되었습니다", saved.getTitle(), "SESSION", saved.getId());
        realtime.toRoom(roomId, "SESSION_STARTED", response, accountId);
        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public SessionResponse getCurrent(Long accountId, Long roomId) {
        access.requireMember(roomId, accountId);
        return repository.findFirstByRoomIdAndStatus(roomId, SessionStatus.IN_PROGRESS).map(s -> toResponse(s, false)).orElse(null);
    }

    @Override
    public SessionResponse close(Long accountId, Long roomId, Long sessionId) {
        access.requireMember(roomId, accountId);
        BoardSession session = find(roomId, sessionId);
        if (session.getStatus() == SessionStatus.CLOSED) {
            throw new BusinessException(SessionErrorCode.SESSION_ALREADY_CLOSED);
        }
        return toResponse(closer.close(session, accountId), false);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<SessionSummaryResponse> getClosed(Long accountId, Long roomId, Pageable pageable) {
        access.requireMember(roomId, accountId);
        return Pages.response(repository.findByRoomIdAndStatus(roomId, SessionStatus.CLOSED, pageable).map(s -> new SessionSummaryResponse(s.getId(), s.getSessionNo(), s.getTitle(),
                cardRepository.countBySessionIdAndDeletedAtIsNull(s.getId()), cardRepository.countWritersBySessionId(s.getId()), s.getClosedAt())));
    }

    @Transactional(readOnly = true)
    @Override
    public SessionResponse getDetail(Long accountId, Long roomId, Long sessionId) {
        access.requireMember(roomId, accountId);
        return toResponse(find(roomId, sessionId), true);
    }

    private BoardSession find(Long roomId, Long sessionId) {
        return repository.findById(sessionId).filter(s -> s.getRoomId().equals(roomId)).orElseThrow(() -> new BusinessException(SessionErrorCode.SESSION_NOT_FOUND));
    }

    private SessionResponse toResponse(BoardSession s, boolean withCounts) {
        return new SessionResponse(s.getId(), s.getSessionNo(), s.getTitle(), s.getStatus(), s.getDeadlineAt(), access.memberRef(s.getRoomId(), s.getStartedByAccountId()), s.getStartedAt(), s.getClosedAt(),
                withCounts ? cardRepository.countBySessionIdAndDeletedAtIsNull(s.getId()) : null, withCounts ? cardRepository.countWritersBySessionId(s.getId()) : null);
    }
}
