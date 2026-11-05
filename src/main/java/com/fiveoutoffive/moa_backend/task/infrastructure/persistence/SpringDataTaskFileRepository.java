package com.fiveoutoffive.moa_backend.task.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTaskFileRepository extends JpaRepository<TaskFileJpaEntity, Long> {
}
