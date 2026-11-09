package com.fiveoutoffive.moa_backend.task.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.task.domain.model.TaskFile;
import com.fiveoutoffive.moa_backend.task.domain.repository.TaskFileRepository;
import com.fiveoutoffive.moa_backend.task.infrastructure.mapper.TaskFileMapper;
import java.util.Collection;
import java.util.List;
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

    @Override
    public List<TaskFile> findByTaskIdOrderBySortOrderAsc(Long taskId) {
        return springDataRepository.findByTaskIdOrderBySortOrderAsc(taskId).stream().map(taskFileMapper::toDomain).toList();
    }

    @Override
    public List<TaskFile> findByTaskIdIn(Collection<Long> taskIds) {
        return springDataRepository.findByTaskIdIn(taskIds).stream().map(taskFileMapper::toDomain).toList();
    }

    @Override
    public Optional<TaskFile> findByTaskIdAndFileId(Long taskId, Long fileId) {
        return springDataRepository.findByTaskIdAndFileId(taskId, fileId).map(taskFileMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }
}
