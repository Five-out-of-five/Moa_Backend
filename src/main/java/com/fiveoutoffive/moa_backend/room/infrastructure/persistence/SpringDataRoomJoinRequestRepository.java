package com.fiveoutoffive.moa_backend.room.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRoomJoinRequestRepository extends JpaRepository<RoomJoinRequestJpaEntity, Long> {
}
