package com.fiveoutoffive.moa_backend.mood.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataMoodStatusRepository extends JpaRepository<MoodStatusJpaEntity, Long> {
}
