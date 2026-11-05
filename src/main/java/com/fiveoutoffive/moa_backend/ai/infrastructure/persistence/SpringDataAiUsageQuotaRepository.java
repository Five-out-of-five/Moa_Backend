package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAiUsageQuotaRepository extends JpaRepository<AiUsageQuotaJpaEntity, Long> {
}
