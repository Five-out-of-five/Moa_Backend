package com.fiveoutoffive.moa_backend.task.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.task.domain.model.Sprint;
import com.fiveoutoffive.moa_backend.task.domain.model.SprintStatus;
import com.fiveoutoffive.moa_backend.task.domain.repository.SprintRepository;
import com.fiveoutoffive.moa_backend.task.infrastructure.mapper.SprintMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
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

    @Override
    public Optional<Sprint> findFirstByRoomIdAndStatus(Long roomId, SprintStatus status) {
        return springDataRepository.findFirstByRoomIdAndStatus(roomId, status).map(sprintMapper::toDomain);
    }

    @Override
    public Page<Sprint> findByRoomIdAndStatus(Long roomId, SprintStatus status, Pageable pageable) {
        return springDataRepository.findByRoomIdAndStatus(roomId, status, pageable).map(sprintMapper::toDomain);
    }

    @Override
    public List<Sprint> findByStatusAndEndDateBefore(SprintStatus status, LocalDate date) {
        return springDataRepository.findByStatusAndEndDateBefore(status, date).stream().map(sprintMapper::toDomain).toList();
    }

    @Override
    public int findMaxSprintNo(@Param("roomId") Long roomId) {
        return springDataRepository.findMaxSprintNo(roomId);
    }
}
