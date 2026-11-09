package com.fiveoutoffive.moa_backend.dashboard.domain.repository;

import com.fiveoutoffive.moa_backend.dashboard.domain.model.RoomWidget;
import com.fiveoutoffive.moa_backend.dashboard.domain.model.WidgetType;
import java.util.List;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface RoomWidgetRepository {

    RoomWidget save(RoomWidget roomWidget);

    Optional<RoomWidget> findById(Long id);

    List<RoomWidget> findByRoomId(Long roomId);

    boolean existsByRoomIdAndWidgetType(Long roomId, WidgetType widgetType);

    void deleteByRoomId(Long roomId);

    void deleteById(Long id);

    void flush();
}
