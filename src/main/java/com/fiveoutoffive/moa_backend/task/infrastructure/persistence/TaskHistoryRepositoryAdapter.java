package com.fiveoutoffive.moa_backend.task.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.task.domain.model.TaskHistory;
import com.fiveoutoffive.moa_backend.task.domain.repository.TaskHistoryRepository;
import com.fiveoutoffive.moa_backend.task.infrastructure.mapper.TaskHistoryMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TaskHistoryRepositoryAdapter implements TaskHistoryRepository {

    private final SpringDataTaskHistoryRepository springDataRepository;
    private final TaskHistoryMapper taskHistoryMapper;

    @Override
    public TaskHistory save(TaskHistory taskHistory) {
        TaskHistoryJpaEntity saved = springDataRepository.save(taskHistoryMapper.toJpaEntity(taskHistory));
        return taskHistoryMapper.toDomain(saved);
    }

    @Override
    public Optional<TaskHistory> findById(Long id) {
        return springDataRepository.findById(id)
                .map(taskHistoryMapper::toDomain);
    }
}
