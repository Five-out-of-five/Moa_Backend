package com.fiveoutoffive.moa_backend.dashboard.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   POST   /api/v1/rooms/{roomId}/dashboard/ai-recommendations  AI 대시보드 구성 추천 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/dashboard/ai-recommendations/apply  AI 구성 적용 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/dashboard/ai-recommendations/undo  AI 구성 되돌리기 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "05. Dashboard", description = "대시보드")
public class DashboardAiController {
}
