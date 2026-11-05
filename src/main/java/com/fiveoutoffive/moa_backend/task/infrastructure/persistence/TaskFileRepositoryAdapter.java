package com.fiveoutoffive.moa_backend.task.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.task.domain.model.TaskFile;
import com.fiveoutoffive.moa_backend.task.domain.repository.TaskFileRepository;
import com.fiveoutoffive.moa_backend.task.infrastructure.mapper.TaskFileMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TaskFileRepositoryAdapter implements TaskFileRepository {

    private final SpringDataTaskFileRepository springDataRepository;
    private final TaskFileMapper taskFileMapper;

    @Override
    public TaskFile save(TaskFile taskFile) {
        TaskFileJpaEntity saved = springDataRepository.save(taskFileMapper.toJpaEntity(taskFile));
        return taskFileMapper.toDomain(saved);
    }

    @Override
    public Optional<TaskFile> findById(Long id) {
        return springDataRepository.findById(id)
                .map(taskFileMapper::toDomain);
    }
}
