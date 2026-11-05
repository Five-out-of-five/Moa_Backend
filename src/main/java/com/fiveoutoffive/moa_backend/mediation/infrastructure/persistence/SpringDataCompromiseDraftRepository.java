package com.fiveoutoffive.moa_backend.mediation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCompromiseDraftRepository extends JpaRepository<CompromiseDraftJpaEntity, Long> {
}
