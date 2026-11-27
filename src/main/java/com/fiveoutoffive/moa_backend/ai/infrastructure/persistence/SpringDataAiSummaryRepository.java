package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataAiSummaryRepository extends JpaRepository<AiSummaryJpaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AiSummaryJpaEntity s where s.id = :id")
    Optional<AiSummaryJpaEntity> findByIdForUpdate(@Param("id") Long id);

    Page<AiSummaryJpaEntity> findByRoomId(Long roomId, Pageable pageable);
}
