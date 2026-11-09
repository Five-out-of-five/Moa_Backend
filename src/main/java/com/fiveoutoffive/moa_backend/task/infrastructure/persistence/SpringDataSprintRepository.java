package com.fiveoutoffive.moa_backend.task.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.task.domain.model.SprintStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataSprintRepository extends JpaRepository<SprintJpaEntity, Long> {

    Optional<SprintJpaEntity> findFirstByRoomIdAndStatus(Long roomId, SprintStatus status);

    Page<SprintJpaEntity> findByRoomIdAndStatus(Long roomId, SprintStatus status, Pageable pageable);

    List<SprintJpaEntity> findByStatusAndEndDateBefore(SprintStatus status, LocalDate date);

    @Query("select coalesce(max(s.sprintNo), 0) from SprintJpaEntity s where s.roomId = :roomId")
    int findMaxSprintNo(@Param("roomId") Long roomId);
}
