package com.fiveoutoffive.moa_backend.task.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTaskFileRepository extends JpaRepository<TaskFileJpaEntity, Long> {

    List<TaskFileJpaEntity> findByTaskIdOrderBySortOrderAsc(Long taskId);

    List<TaskFileJpaEntity> findByTaskIdIn(Collection<Long> taskIds);

    Optional<TaskFileJpaEntity> findByTaskIdAndFileId(Long taskId, Long fileId);

    void deleteById(Long id);
}
