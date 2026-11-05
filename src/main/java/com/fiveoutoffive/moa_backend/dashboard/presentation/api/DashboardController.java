package com.fiveoutoffive.moa_backend.dashboard.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/rooms/{roomId}/dashboard  대시보드 레이아웃 조회 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/dashboard/widgets  위젯 추가 [방 멤버]
//   DELETE /api/v1/rooms/{roomId}/dashboard/widgets/{widgetId}  위젯 제거 [방 멤버]
//   PUT    /api/v1/rooms/{roomId}/dashboard/layout  위젯 배치·크기 저장 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/dashboard/reset  레이아웃 초기화 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "05. Dashboard", description = "대시보드")
public class DashboardController {
}
