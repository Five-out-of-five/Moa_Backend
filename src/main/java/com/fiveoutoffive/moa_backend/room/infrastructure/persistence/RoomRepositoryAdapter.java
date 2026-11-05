package com.fiveoutoffive.moa_backend.room.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.room.domain.model.Room;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomRepository;
import com.fiveoutoffive.moa_backend.room.infrastructure.mapper.RoomMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoomRepositoryAdapter implements RoomRepository {

    private final SpringDataRoomRepository springDataRepository;
    private final RoomMapper roomMapper;

    @Override
    public Room save(Room room) {
        RoomJpaEntity saved = springDataRepository.save(roomMapper.toJpaEntity(room));
        return roomMapper.toDomain(saved);
    }

    @Override
    public Optional<Room> findById(Long id) {
        return springDataRepository.findById(id)
                .map(roomMapper::toDomain);
    }
}
