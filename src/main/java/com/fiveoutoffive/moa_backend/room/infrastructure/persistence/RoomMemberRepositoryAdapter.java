package com.fiveoutoffive.moa_backend.room.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.room.domain.model.RoomMember;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberStatus;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomMemberRepository;
import com.fiveoutoffive.moa_backend.room.infrastructure.mapper.RoomMemberMapper;
import java.util.Collection;
import java.util.List;
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

    @Override
    public Optional<RoomMember> findByRoomIdAndAccountId(Long roomId, Long accountId) {
        return springDataRepository.findByRoomIdAndAccountId(roomId, accountId).map(roomMemberMapper::toDomain);
    }

    @Override
    public List<RoomMember> findByAccountIdAndStatus(Long accountId, RoomMemberStatus status) {
        return springDataRepository.findByAccountIdAndStatus(accountId, status).stream().map(roomMemberMapper::toDomain).toList();
    }

    @Override
    public List<RoomMember> findByRoomIdAndStatus(Long roomId, RoomMemberStatus status) {
        return springDataRepository.findByRoomIdAndStatus(roomId, status).stream().map(roomMemberMapper::toDomain).toList();
    }

    @Override
    public List<RoomMember> findByRoomIdAndAccountIdIn(Long roomId, Collection<Long> accountIds) {
        return springDataRepository.findByRoomIdAndAccountIdIn(roomId, accountIds).stream().map(roomMemberMapper::toDomain).toList();
    }

    @Override
    public long countByRoomIdAndStatus(Long roomId, RoomMemberStatus status) {
        return springDataRepository.countByRoomIdAndStatus(roomId, status);
    }

    @Override
    public boolean existsByRoomIdAndNicknameAndStatus(Long roomId, String nickname, RoomMemberStatus status) {
        return springDataRepository.existsByRoomIdAndNicknameAndStatus(roomId, nickname, status);
    }

    @Override
    public RoomMember saveAndFlush(RoomMember roomMember) {
        return roomMemberMapper.toDomain(springDataRepository.saveAndFlush(roomMemberMapper.toJpaEntity(roomMember)));
    }
}
