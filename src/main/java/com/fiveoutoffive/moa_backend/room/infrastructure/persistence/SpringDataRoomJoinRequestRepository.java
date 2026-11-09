package com.fiveoutoffive.moa_backend.room.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.room.domain.model.JoinRequestStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRoomJoinRequestRepository extends JpaRepository<RoomJoinRequestJpaEntity, Long> {

    Optional<RoomJoinRequestJpaEntity> findByRoomIdAndAccountIdAndStatus(Long roomId, Long accountId, JoinRequestStatus status);

    Page<RoomJoinRequestJpaEntity> findByRoomIdAndStatus(Long roomId, JoinRequestStatus status, Pageable pageable);

    List<RoomJoinRequestJpaEntity> findByStatusAndExpiresAtBefore(JoinRequestStatus status, LocalDateTime time);
}
