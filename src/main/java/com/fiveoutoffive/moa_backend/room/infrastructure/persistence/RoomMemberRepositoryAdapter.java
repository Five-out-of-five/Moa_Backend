package com.fiveoutoffive.moa_backend.room.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.room.domain.model.RoomMember;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomMemberRepository;
import com.fiveoutoffive.moa_backend.room.infrastructure.mapper.RoomMemberMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoomMemberRepositoryAdapter implements RoomMemberRepository {

    private final SpringDataRoomMemberRepository springDataRepository;
    private final RoomMemberMapper roomMemberMapper;

    @Override
    public RoomMember save(RoomMember roomMember) {
        RoomMemberJpaEntity saved = springDataRepository.save(roomMemberMapper.toJpaEntity(roomMember));
        return roomMemberMapper.toDomain(saved);
    }

    @Override
    public Optional<RoomMember> findById(Long id) {
        return springDataRepository.findById(id)
                .map(roomMemberMapper::toDomain);
    }
}
