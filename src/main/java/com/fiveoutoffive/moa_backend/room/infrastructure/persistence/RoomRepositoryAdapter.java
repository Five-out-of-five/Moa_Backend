package com.fiveoutoffive.moa_backend.room.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.room.domain.model.Room;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomStatus;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomRepository;
import com.fiveoutoffive.moa_backend.room.infrastructure.mapper.RoomMapper;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
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

    @Override
    public Optional<Room> findLockedById(@Param("id") Long id) {
        return springDataRepository.findLockedById(id).map(roomMapper::toDomain);
    }

    @Override
    public Optional<Room> findByInviteCodeAndDeletedAtIsNull(String inviteCode) {
        return springDataRepository.findByInviteCodeAndDeletedAtIsNull(inviteCode).map(roomMapper::toDomain);
    }

    @Override
    public long countByCreatedByAccountIdAndDeletedAtIsNull(Long accountId) {
        return springDataRepository.countByCreatedByAccountIdAndDeletedAtIsNull(accountId);
    }

    @Override
    public List<Room> findByIdInAndDeletedAtIsNull(Collection<Long> ids) {
        return springDataRepository.findByIdInAndDeletedAtIsNull(ids).stream().map(roomMapper::toDomain).toList();
    }

    @Override
    public List<Room> findByDeletedAtBefore(LocalDateTime time) {
        return springDataRepository.findByDeletedAtBefore(time).stream().map(roomMapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public long countByDeletedAtIsNull() {
        return springDataRepository.countByDeletedAtIsNull();
    }

    @Override
    public long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from, LocalDateTime to) {
        return springDataRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(from, to);
    }

    @Override
    public Page<Room> search(@Param("keyword") String keyword, @Param("status") RoomStatus status, @Param("inactiveBefore") LocalDateTime inactiveBefore, Pageable pageable) {
        return springDataRepository.search(keyword, status, inactiveBefore, pageable).map(roomMapper::toDomain);
    }
}
