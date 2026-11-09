package com.fiveoutoffive.moa_backend.task.domain.repository;

import com.fiveoutoffive.moa_backend.task.domain.model.Task;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface TaskRepository {

    Task save(Task task);

    Optional<Task> findById(Long id);

    List<Task> findBySourceCardIdInAndDeletedAtIsNull(Collection<Long> cardIds);

    boolean existsBySourceCardIdAndDeletedAtIsNull(Long cardId);

    List<Task> findByRoomIdAndDeletedAtIsNull(Long roomId);

    List<Task> findByRoomIdAndStatusAndDeletedAtIsNullOrderBySortOrderAsc(Long roomId, TaskStatus status);

    List<Task> findBySprintIdAndDeletedAtIsNull(Long sprintId);

    List<Task> findByRoomIdAndAssigneeAccountIdAndDeletedAtIsNull(Long roomId, Long assigneeAccountId);

    List<Task> findByRoomIdAndRegistrantAccountIdAndAssigneeAccountIdIsNullAndDeletedAtIsNull(Long roomId, Long registrantAccountId);

    long countByRoomIdAndAssigneeAccountIdAndStatusNotAndDeletedAtIsNull(Long roomId, Long assigneeAccountId, TaskStatus status);

    int findMaxSortOrder(@Param("roomId") Long roomId, @Param("status") TaskStatus status);

    List<Task> findDueSoon(@Param("date") LocalDate date, @Param("done") TaskStatus done);
}
