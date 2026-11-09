package com.fiveoutoffive.moa_backend.room.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRoomMemberRepository extends JpaRepository<RoomMemberJpaEntity, Long> {

    Optional<RoomMemberJpaEntity> findByRoomIdAndAccountId(Long roomId, Long accountId);

    List<RoomMemberJpaEntity> findByAccountIdAndStatus(Long accountId, RoomMemberStatus status);

    List<RoomMemberJpaEntity> findByRoomIdAndStatus(Long roomId, RoomMemberStatus status);

    List<RoomMemberJpaEntity> findByRoomIdAndAccountIdIn(Long roomId, Collection<Long> accountIds);

    long countByRoomIdAndStatus(Long roomId, RoomMemberStatus status);

    boolean existsByRoomIdAndNicknameAndStatus(Long roomId, String nickname, RoomMemberStatus status);
}
