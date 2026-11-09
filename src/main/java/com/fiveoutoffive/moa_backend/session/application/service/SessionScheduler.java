package com.fiveoutoffive.moa_backend.session.application.service;

import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.session.domain.model.SessionStatus;
import com.fiveoutoffive.moa_backend.session.domain.repository.BoardSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SessionScheduler {

    private final BoardSessionRepository repository;
    private final SessionCloser closer;

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void closeExpired() {
        repository.findByStatusAndDeadlineAtBefore(SessionStatus.IN_PROGRESS, KstTime.nowUtc()).forEach(session -> closer.close(session, null));
    }
}
