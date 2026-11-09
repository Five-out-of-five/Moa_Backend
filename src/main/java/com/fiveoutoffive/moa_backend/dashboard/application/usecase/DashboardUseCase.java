package com.fiveoutoffive.moa_backend.dashboard.application.usecase;

import com.fiveoutoffive.moa_backend.dashboard.application.command.WidgetPlacement;
import com.fiveoutoffive.moa_backend.dashboard.application.result.DashboardResponse;
import com.fiveoutoffive.moa_backend.dashboard.application.result.LayoutRecommendResponse;
import com.fiveoutoffive.moa_backend.room.domain.event.RoomCreatedEvent;
import java.util.List;

public interface DashboardUseCase {

    DashboardResponse get(Long accountId, Long roomId);

    DashboardResponse addWidget(Long accountId, Long roomId, WidgetPlacement placement);

    DashboardResponse removeWidget(Long accountId, Long roomId, Long widgetId);

    DashboardResponse updateLayout(Long accountId, Long roomId, List<WidgetPlacement> placements);

    DashboardResponse reset(Long accountId, Long roomId);

    LayoutRecommendResponse recommend(Long accountId, Long roomId, String purpose);

    DashboardResponse applyRecommendation(Long accountId, Long roomId, List<WidgetPlacement> placements);

    DashboardResponse undoRecommendation(Long accountId, Long roomId);

    void onRoomCreated(RoomCreatedEvent event);
}
