package com.fiveoutoffive.moa_backend.dashboard.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRoomLayoutSnapshotRepository extends JpaRepository<RoomLayoutSnapshotJpaEntity, Long> {

    Optional<RoomLayoutSnapshotJpaEntity> findByRoomId(Long roomId);

    void deleteByRoomId(Long roomId);
}
