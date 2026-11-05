package com.fiveoutoffive.moa_backend.task.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.task.domain.model.Task;
import com.fiveoutoffive.moa_backend.task.domain.repository.TaskRepository;
import com.fiveoutoffive.moa_backend.task.infrastructure.mapper.TaskMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
}
