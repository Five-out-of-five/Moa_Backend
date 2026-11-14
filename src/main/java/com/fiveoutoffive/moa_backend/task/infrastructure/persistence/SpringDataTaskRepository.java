package com.fiveoutoffive.moa_backend.task.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.task.domain.model.TaskStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataTaskRepository extends JpaRepository<TaskJpaEntity, Long> {

    List<TaskJpaEntity> findBySourceCardIdInAndDeletedAtIsNull(Collection<Long> cardIds);

    boolean existsBySourceCardIdAndDeletedAtIsNull(Long cardId);

    List<TaskJpaEntity> findByRoomIdAndDeletedAtIsNull(Long roomId);

    List<TaskJpaEntity> findByRoomIdAndStatusAndDeletedAtIsNullOrderBySortOrderAsc(Long roomId, TaskStatus status);

    List<TaskJpaEntity> findBySprintIdAndDeletedAtIsNull(Long sprintId);

    List<TaskJpaEntity> findByRoomIdAndAssigneeAccountIdAndDeletedAtIsNull(Long roomId, Long assigneeAccountId);

    List<TaskJpaEntity> findByRoomIdAndRegistrantAccountIdAndAssigneeAccountIdIsNullAndDeletedAtIsNull(Long roomId, Long registrantAccountId);

    long countByRoomIdAndAssigneeAccountIdAndStatusNotAndDeletedAtIsNull(Long roomId, Long assigneeAccountId, TaskStatus status);

    // JPQL 일괄 update 는 versioned 를 쓰지 않으면 @Version 을 올리지 않는다.
    @Modifying
    @Query("update TaskJpaEntity t set t.sortOrder = :sortOrder where t.id = :id")
    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") int sortOrder);

    @Query("select coalesce(max(t.sortOrder), -1) from TaskJpaEntity t where t.roomId = :roomId and t.status = :status and t.deletedAt is null")
    int findMaxSortOrder(@Param("roomId") Long roomId, @Param("status") TaskStatus status);

    @Query("select t from TaskJpaEntity t where t.deletedAt is null and t.status <> :done and t.dueDate is not null and t.dueDate <= :date and t.dueAlertSentAt is null and t.assigneeAccountId is not null")
    List<TaskJpaEntity> findDueSoon(@Param("date") LocalDate date, @Param("done") TaskStatus done);
}
