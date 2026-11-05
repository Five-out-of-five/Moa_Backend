package com.fiveoutoffive.moa_backend.mediation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCardConflictRepository extends JpaRepository<CardConflictJpaEntity, Long> {
}
