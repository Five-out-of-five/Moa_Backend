package com.fiveoutoffive.moa_backend.ai.application.service;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiCallLog;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiCallStatus;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiUsageQuota;
import com.fiveoutoffive.moa_backend.ai.domain.repository.AiUsageQuotaRepository;
import com.fiveoutoffive.moa_backend.ai.exception.AiErrorCode;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.port.out.AiServerPort;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 모든 AI 호출의 단일 창구: 방별 일일 한도 확인 → Python 서버 호출 → ai_call_log 기록. */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class AiGateway {

    private final AiServerPort aiServerPort;
    private final AiUsageQuotaRepository quotaRepository;
    private final AiCallLogWriter callLogWriter;
    private final AiQuotaProperties quotaProperties;

    /** 사용자가 직접 호출하는 기능. 한도 초과는 429, 서버 불가는 503 으로 응답한다. */
    public <T> T call(Long roomId, Long accountId, AiFeature feature, String path, Object body, Class<T> type) {
        requireQuota(roomId, feature);
        return tryCall(roomId, accountId, feature, path, body, type).orElseThrow(() -> new BusinessException(AiErrorCode.AI_UNAVAILABLE));
    }

    /** 실패해도 흐름을 막지 않는 기능(톤앤매너, 자동 충돌 감지). 한도를 넘으면 빈 값이다. */
    public <T> Optional<T> callQuietly(Long roomId, Long accountId, AiFeature feature, String path, Object body, Class<T> type) {
        if (remaining(roomId, feature) <= 0) {
            return Optional.empty();
        }
        return tryCall(roomId, accountId, feature, path, body, type);
    }

    public void requireQuota(Long roomId, AiFeature feature) {
        if (remaining(roomId, feature) <= 0) {
            throw new BusinessException(AiErrorCode.AI_QUOTA_EXCEEDED);
        }
    }

    public int remaining(Long roomId, AiFeature feature) {
        int used = quotaRepository.findByRoomIdAndFeatureAndQuotaDate(roomId, feature, KstTime.todayKst()).map(AiUsageQuota::getUsedCount).orElse(0);
        return quotaProperties.limitOf(feature) - used;
    }

    public void consume(Long roomId, AiFeature feature) {
        quotaRepository.increment(roomId, feature.name(), KstTime.todayKst());
    }

    public void log(Long roomId, Long accountId, AiFeature feature, long startedAt, AiCallStatus status, String error) {
        callLogWriter.write(AiCallLog.builder().feature(feature).roomId(roomId).accountId(accountId).latencyMs((int) (System.currentTimeMillis() - startedAt)).status(status).errorMessage(error).build());
    }

    private <T> Optional<T> tryCall(Long roomId, Long accountId, AiFeature feature, String path, Object body, Class<T> type) {
        long startedAt = System.currentTimeMillis();
        Optional<T> result = aiServerPort.post(path, body, type);
        if (result.isPresent()) {
            consume(roomId, feature);
        }
        log(roomId, accountId, feature, startedAt, result.isPresent() ? AiCallStatus.SUCCESS : AiCallStatus.FAILED, result.isPresent() ? null : "AI server unavailable");
        return result;
    }
}
