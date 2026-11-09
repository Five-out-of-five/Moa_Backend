package com.fiveoutoffive.moa_backend.room.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.room.domain.model.JoinRequestStatus;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomJoinRequest;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomJoinRequestRepository;
import com.fiveoutoffive.moa_backend.room.infrastructure.mapper.RoomJoinRequestMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    public Optional<RoomJoinRequest> findByRoomIdAndAccountIdAndStatus(Long roomId, Long accountId, JoinRequestStatus status) {
        return springDataRepository.findByRoomIdAndAccountIdAndStatus(roomId, accountId, status).map(roomJoinRequestMapper::toDomain);
    }

    @Override
    public Page<RoomJoinRequest> findByRoomIdAndStatus(Long roomId, JoinRequestStatus status, Pageable pageable) {
        return springDataRepository.findByRoomIdAndStatus(roomId, status, pageable).map(roomJoinRequestMapper::toDomain);
    }

    @Override
    public List<RoomJoinRequest> findByStatusAndExpiresAtBefore(JoinRequestStatus status, LocalDateTime time) {
        return springDataRepository.findByStatusAndExpiresAtBefore(status, time).stream().map(roomJoinRequestMapper::toDomain).toList();
    }
}
