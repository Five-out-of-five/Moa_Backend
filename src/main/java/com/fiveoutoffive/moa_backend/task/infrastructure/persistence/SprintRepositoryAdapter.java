package com.fiveoutoffive.moa_backend.task.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.task.domain.model.Sprint;
import com.fiveoutoffive.moa_backend.task.domain.repository.SprintRepository;
import com.fiveoutoffive.moa_backend.task.infrastructure.mapper.SprintMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SprintRepositoryAdapter implements SprintRepository {

    private final SpringDataSprintRepository springDataRepository;
    private final SprintMapper sprintMapper;

    @Override
    public Sprint save(Sprint sprint) {
        SprintJpaEntity saved = springDataRepository.save(sprintMapper.toJpaEntity(sprint));
        return sprintMapper.toDomain(saved);
    }

    @Override
    public Optional<Sprint> findById(Long id) {
        return springDataRepository.findById(id)
                .map(sprintMapper::toDomain);
    }
}
