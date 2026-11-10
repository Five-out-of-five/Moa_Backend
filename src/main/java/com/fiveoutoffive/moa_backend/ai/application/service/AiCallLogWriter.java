package com.fiveoutoffive.moa_backend.ai.application.service;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiCallLog;
import com.fiveoutoffive.moa_backend.ai.domain.repository.AiCallLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * ai_call_log 를 호출한 쪽 트랜잭션과 따로 저장한다.
 * AI 호출이 실패하면 호출한 서비스가 AI_UNAVAILABLE 예외로 롤백되는데, 같은 트랜잭션이면 실패 기록까지 지워진다.
 * API 명세: 모든 호출은 ai_call_log 에 남긴다(관리자 AI 사용량의 실패 건수).
 */
@Component
@RequiredArgsConstructor
public class AiCallLogWriter {

    private final AiCallLogRepository callLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(AiCallLog log) {
        callLogRepository.save(log);
    }
}
