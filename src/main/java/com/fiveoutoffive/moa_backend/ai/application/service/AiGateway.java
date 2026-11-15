package com.fiveoutoffive.moa_backend.ai.application.service;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiCallLog;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiCallStatus;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import com.fiveoutoffive.moa_backend.ai.domain.repository.AiUsageQuotaRepository;
import com.fiveoutoffive.moa_backend.ai.exception.AiErrorCode;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.port.out.AiServerPort;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 모든 AI 호출의 단일 창구: 방별 일일 한도 선점 → Python 서버 호출 → ai_call_log 기록.
 *
 * <p>한도는 호출 전에 1회를 먼저 잡고(선점), 호출이 실패하면 돌려준다(반납). 확인과 차감 사이가 벌어지면
 * 같은 방의 동시 요청이 한도를 넘는다. 결과적으로 "성공한 호출만 차감" 은 그대로다.
 *
 * <p>트랜잭션을 걸지 않는다. AI 응답을 기다리는 동안 DB 연결을 붙잡지 않기 위해서다.
 * 선점·반납은 저장소에서, 호출 로그는 AiCallLogWriter 에서 각각 바로 커밋한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiGateway {

    private final AiServerPort aiServerPort;
    private final AiUsageQuotaRepository quotaRepository;
    private final AiCallLogWriter callLogWriter;
    private final AiQuotaProperties quotaProperties;

    /** 사용자가 직접 호출하는 기능. 한도 초과는 429, 서버 불가는 503 으로 응답한다. */
    public <T> T call(Long roomId, Long accountId, AiFeature feature, String path, Object body, Class<T> type) {
        LocalDate quotaDate = reserve(roomId, feature);
        return callReserved(roomId, accountId, feature, quotaDate, path, body, type)
                .orElseThrow(() -> new BusinessException(AiErrorCode.AI_UNAVAILABLE));
    }

    /** 실패해도 흐름을 막지 않는 기능(자동 충돌 감지). 한도를 넘었거나 실패하면 빈 값이다. */
    public <T> Optional<T> callQuietly(Long roomId, Long accountId, AiFeature feature, String path, Object body, Class<T> type) {
        return tryReserve(roomId, feature).flatMap(quotaDate -> callReserved(roomId, accountId, feature, quotaDate, path, body, type));
    }

    /** 이미 선점한 1회로 호출한다. 실패하면 반납하고 빈 값이다. */
    public <T> Optional<T> callReserved(Long roomId, Long accountId, AiFeature feature, LocalDate quotaDate, String path, Object body, Class<T> type) {
        long startedAt = System.currentTimeMillis();
        Optional<T> result = aiServerPort.post(path, body, type);
        if (result.isEmpty()) {
            release(roomId, feature, quotaDate);
        }
        log(roomId, accountId, feature, startedAt, result.isPresent() ? AiCallStatus.SUCCESS : AiCallStatus.FAILED, result.isPresent() ? null : "AI server unavailable");
        return result;
    }

    /** 1회를 선점하고 선점한 날짜(KST)를 돌려준다. 한도면 429. 반납할 때 이 날짜를 넘긴다. */
    public LocalDate reserve(Long roomId, AiFeature feature) {
        return tryReserve(roomId, feature).orElseThrow(() -> new BusinessException(AiErrorCode.AI_QUOTA_EXCEEDED));
    }

    public Optional<LocalDate> tryReserve(Long roomId, AiFeature feature) {
        LocalDate today = KstTime.todayKst();
        int limit = quotaProperties.limitOf(feature);
        // 행이 없을 때의 insert 는 한도를 보지 않으므로 한도 0 인 기능(TASK_RECOMMEND)은 여기서 막는다.
        return limit > 0 && quotaRepository.tryReserve(roomId, feature, today, limit) ? Optional.of(today) : Optional.empty();
    }

    public void release(Long roomId, AiFeature feature, LocalDate quotaDate) {
        quotaRepository.release(roomId, feature, quotaDate);
    }

    public void log(Long roomId, Long accountId, AiFeature feature, long startedAt, AiCallStatus status, String error) {
        callLogWriter.write(AiCallLog.builder().feature(feature).roomId(roomId).accountId(accountId).latencyMs((int) (System.currentTimeMillis() - startedAt)).status(status).errorMessage(error).build());
    }
}
