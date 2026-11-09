package com.fiveoutoffive.moa_backend.task.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.task.domain.model.Task;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskStatus;
import com.fiveoutoffive.moa_backend.task.domain.repository.TaskRepository;
import com.fiveoutoffive.moa_backend.task.infrastructure.mapper.TaskMapper;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TaskRepositoryAdapter implements TaskRepository {

    private final SpringDataTaskRepository springDataRepository;
    private final TaskMapper taskMapper;

    @Override
    public Task save(Task task) {
        TaskJpaEntity saved = springDataRepository.save(taskMapper.toJpaEntity(task));
        return taskMapper.toDomain(saved);
    }

    @Override
    public Optional<Task> findById(Long id) {
        return springDataRepository.findById(id)
                .map(taskMapper::toDomain);
    }

    @Override
    public List<Task> findBySourceCardIdInAndDeletedAtIsNull(Collection<Long> cardIds) {
        return springDataRepository.findBySourceCardIdInAndDeletedAtIsNull(cardIds).stream().map(taskMapper::toDomain).toList();
    }

    @Override
    public boolean existsBySourceCardIdAndDeletedAtIsNull(Long cardId) {
        return springDataRepository.existsBySourceCardIdAndDeletedAtIsNull(cardId);
    }

    @Override
    public List<Task> findByRoomIdAndDeletedAtIsNull(Long roomId) {
        return springDataRepository.findByRoomIdAndDeletedAtIsNull(roomId).stream().map(taskMapper::toDomain).toList();
    }

    @Override
    public List<Task> findByRoomIdAndStatusAndDeletedAtIsNullOrderBySortOrderAsc(Long roomId, TaskStatus status) {
        return springDataRepository.findByRoomIdAndStatusAndDeletedAtIsNullOrderBySortOrderAsc(roomId, status).stream().map(taskMapper::toDomain).toList();
    }

    @Override
    public List<Task> findBySprintIdAndDeletedAtIsNull(Long sprintId) {
        return springDataRepository.findBySprintIdAndDeletedAtIsNull(sprintId).stream().map(taskMapper::toDomain).toList();
    }

    @Override
    public List<Task> findByRoomIdAndAssigneeAccountIdAndDeletedAtIsNull(Long roomId, Long assigneeAccountId) {
        return springDataRepository.findByRoomIdAndAssigneeAccountIdAndDeletedAtIsNull(roomId, assigneeAccountId).stream().map(taskMapper::toDomain).toList();
    }

    @Override
    public List<Task> findByRoomIdAndRegistrantAccountIdAndAssigneeAccountIdIsNullAndDeletedAtIsNull(Long roomId, Long registrantAccountId) {
        return springDataRepository.findByRoomIdAndRegistrantAccountIdAndAssigneeAccountIdIsNullAndDeletedAtIsNull(roomId, registrantAccountId).stream().map(taskMapper::toDomain).toList();
    }

    @Override
    public long countByRoomIdAndAssigneeAccountIdAndStatusNotAndDeletedAtIsNull(Long roomId, Long assigneeAccountId, TaskStatus status) {
        return springDataRepository.countByRoomIdAndAssigneeAccountIdAndStatusNotAndDeletedAtIsNull(roomId, assigneeAccountId, status);
    }

    @Override
    public int findMaxSortOrder(@Param("roomId") Long roomId, @Param("status") TaskStatus status) {
        return springDataRepository.findMaxSortOrder(roomId, status);
    }

    @Override
    public List<Task> findDueSoon(@Param("date") LocalDate date, @Param("done") TaskStatus done) {
        return springDataRepository.findDueSoon(date, done).stream().map(taskMapper::toDomain).toList();
    }
}
