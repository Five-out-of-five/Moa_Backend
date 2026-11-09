package com.fiveoutoffive.moa_backend.task.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.task.domain.model.TaskHistoryType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTaskHistoryRepository extends JpaRepository<TaskHistoryJpaEntity, Long> {

    List<TaskHistoryJpaEntity> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    Optional<TaskHistoryJpaEntity> findFirstByTaskIdAndHistoryTypeOrderByCreatedAtDesc(Long taskId, TaskHistoryType historyType);
}
