package com.fiveoutoffive.moa_backend.task.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSprintRepository extends JpaRepository<SprintJpaEntity, Long> {
}
