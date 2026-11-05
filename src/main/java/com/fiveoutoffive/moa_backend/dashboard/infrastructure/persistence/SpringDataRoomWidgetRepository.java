package com.fiveoutoffive.moa_backend.dashboard.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRoomWidgetRepository extends JpaRepository<RoomWidgetJpaEntity, Long> {
}
