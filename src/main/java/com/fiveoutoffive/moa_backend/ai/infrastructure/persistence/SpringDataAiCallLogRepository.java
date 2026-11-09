package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAiCallLogRepository extends JpaRepository<AiCallLogJpaEntity, Long> {

    List<AiCallLogJpaEntity> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from, LocalDateTime to);
}
