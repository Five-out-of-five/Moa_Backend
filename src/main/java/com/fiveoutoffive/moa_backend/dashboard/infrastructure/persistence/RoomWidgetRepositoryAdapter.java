package com.fiveoutoffive.moa_backend.dashboard.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.dashboard.domain.model.RoomWidget;
import com.fiveoutoffive.moa_backend.dashboard.domain.model.WidgetType;
import com.fiveoutoffive.moa_backend.dashboard.domain.repository.RoomWidgetRepository;
import com.fiveoutoffive.moa_backend.dashboard.infrastructure.mapper.RoomWidgetMapper;
import java.util.List;
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

    @Override
    public List<RoomWidget> findByRoomId(Long roomId) {
        return springDataRepository.findByRoomId(roomId).stream().map(roomWidgetMapper::toDomain).toList();
    }

    @Override
    public boolean existsByRoomIdAndWidgetType(Long roomId, WidgetType widgetType) {
        return springDataRepository.existsByRoomIdAndWidgetType(roomId, widgetType);
    }

    @Override
    public void deleteByRoomId(Long roomId) {
        springDataRepository.deleteByRoomId(roomId);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public void flush() {
        springDataRepository.flush();
    }
}
