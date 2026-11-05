package com.fiveoutoffive.moa_backend.dashboard.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.dashboard.domain.model.RoomWidget;
import com.fiveoutoffive.moa_backend.dashboard.domain.repository.RoomWidgetRepository;
import com.fiveoutoffive.moa_backend.dashboard.infrastructure.mapper.RoomWidgetMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoomWidgetRepositoryAdapter implements RoomWidgetRepository {

    private final SpringDataRoomWidgetRepository springDataRepository;
    private final RoomWidgetMapper roomWidgetMapper;

    @Override
    public RoomWidget save(RoomWidget roomWidget) {
        RoomWidgetJpaEntity saved = springDataRepository.save(roomWidgetMapper.toJpaEntity(roomWidget));
        return roomWidgetMapper.toDomain(saved);
    }

    @Override
    public Optional<RoomWidget> findById(Long id) {
        return springDataRepository.findById(id)
                .map(roomWidgetMapper::toDomain);
    }
}
