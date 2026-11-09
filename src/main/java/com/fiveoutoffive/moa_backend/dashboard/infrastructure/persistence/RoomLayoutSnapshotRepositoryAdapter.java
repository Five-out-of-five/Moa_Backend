package com.fiveoutoffive.moa_backend.dashboard.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.dashboard.domain.model.RoomLayoutSnapshot;
import com.fiveoutoffive.moa_backend.dashboard.domain.repository.RoomLayoutSnapshotRepository;
import com.fiveoutoffive.moa_backend.dashboard.infrastructure.mapper.RoomLayoutSnapshotMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoomLayoutSnapshotRepositoryAdapter implements RoomLayoutSnapshotRepository {

    private final SpringDataRoomLayoutSnapshotRepository springDataRepository;
    private final RoomLayoutSnapshotMapper roomLayoutSnapshotMapper;

    @Override
    public RoomLayoutSnapshot save(RoomLayoutSnapshot roomLayoutSnapshot) {
        RoomLayoutSnapshotJpaEntity saved = springDataRepository.save(roomLayoutSnapshotMapper.toJpaEntity(roomLayoutSnapshot));
        return roomLayoutSnapshotMapper.toDomain(saved);
    }

    @Override
    public Optional<RoomLayoutSnapshot> findById(Long id) {
        return springDataRepository.findById(id)
                .map(roomLayoutSnapshotMapper::toDomain);
    }

    @Override
    public Optional<RoomLayoutSnapshot> findByRoomId(Long roomId) {
        return springDataRepository.findByRoomId(roomId).map(roomLayoutSnapshotMapper::toDomain);
    }

    @Override
    public void deleteByRoomId(Long roomId) {
        springDataRepository.deleteByRoomId(roomId);
    }

    @Override
    public void flush() {
        springDataRepository.flush();
    }
}
