package com.fiveoutoffive.moa_backend.integration.application.service;

import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.integration.domain.model.NotionExportLog;
import com.fiveoutoffive.moa_backend.integration.domain.model.NotionIntegration;
import com.fiveoutoffive.moa_backend.integration.domain.repository.NotionExportLogRepository;
import com.fiveoutoffive.moa_backend.integration.domain.repository.NotionIntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 노션 호출 실패의 흔적(토큰 만료 표시, 내보내기 실패 이력)을 호출한 쪽 트랜잭션과 따로 저장한다.
 * 호출한 쪽은 바로 AUTH_EXPIRED·DATABASE_NOT_FOUND 예외를 던져 롤백되므로, 같은 트랜잭션이면 이 기록이 사라진다.
 */
@Component
@RequiredArgsConstructor
public class NotionFailureWriter {

    private final NotionIntegrationRepository repository;
    private final NotionExportLogRepository logRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markTokenExpired(NotionIntegration integration) {
        repository.save(integration.toBuilder().tokenExpiredAt(KstTime.nowUtc()).build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(NotionExportLog log) {
        logRepository.save(log);
    }
}
