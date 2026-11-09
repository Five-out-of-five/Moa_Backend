package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAiSummaryRepository extends JpaRepository<AiSummaryJpaEntity, Long> {

    Page<AiSummaryJpaEntity> findByRoomId(Long roomId, Pageable pageable);
}
