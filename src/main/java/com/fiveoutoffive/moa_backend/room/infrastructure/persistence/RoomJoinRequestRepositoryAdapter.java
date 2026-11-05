package com.fiveoutoffive.moa_backend.room.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.room.domain.model.RoomJoinRequest;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomJoinRequestRepository;
import com.fiveoutoffive.moa_backend.room.infrastructure.mapper.RoomJoinRequestMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoomJoinRequestRepositoryAdapter implements RoomJoinRequestRepository {

    private final SpringDataRoomJoinRequestRepository springDataRepository;
    private final RoomJoinRequestMapper roomJoinRequestMapper;

    @Override
    public RoomJoinRequest save(RoomJoinRequest roomJoinRequest) {
        RoomJoinRequestJpaEntity saved = springDataRepository.save(roomJoinRequestMapper.toJpaEntity(roomJoinRequest));
        return roomJoinRequestMapper.toDomain(saved);
    }

    @Override
    public Optional<RoomJoinRequest> findById(Long id) {
        return springDataRepository.findById(id)
                .map(roomJoinRequestMapper::toDomain);
    }
}
