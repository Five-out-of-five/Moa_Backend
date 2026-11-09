package com.fiveoutoffive.moa_backend.ai.domain.repository;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiCallLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface AiCallLogRepository {

    AiCallLog save(AiCallLog aiCallLog);

    Optional<AiCallLog> findById(Long id);

    List<AiCallLog> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from, LocalDateTime to);
}
