package com.fiveoutoffive.moa_backend.briefing.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/rooms/{roomId}/briefing  캐치업 브리핑 조회 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/briefing/check  브리핑 모두 확인 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/briefing/dismiss  브리핑 항목 개별 소거 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "11. Briefing", description = "캐치업 브리핑")
public class BriefingController {
}
