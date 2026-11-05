package com.fiveoutoffive.moa_backend.briefing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBriefingDismissalRepository extends JpaRepository<BriefingDismissalJpaEntity, Long> {
}
