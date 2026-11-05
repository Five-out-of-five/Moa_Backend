package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAiSummarySessionRepository extends JpaRepository<AiSummarySessionJpaEntity, AiSummarySessionJpaId> {
}
