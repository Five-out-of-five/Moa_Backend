package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAiSummarySessionRepository extends JpaRepository<AiSummarySessionJpaEntity, AiSummarySessionJpaId> {

    List<AiSummarySessionJpaEntity> findBySummaryId(Long summaryId);
}
