package com.fiveoutoffive.moa_backend.dashboard.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.dashboard.domain.model.WidgetType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRoomWidgetRepository extends JpaRepository<RoomWidgetJpaEntity, Long> {

    List<RoomWidgetJpaEntity> findByRoomId(Long roomId);

    boolean existsByRoomIdAndWidgetType(Long roomId, WidgetType widgetType);

    void deleteByRoomId(Long roomId);

    void deleteById(Long id);
}
