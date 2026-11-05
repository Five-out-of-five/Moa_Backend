package com.fiveoutoffive.moa_backend.mood.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   PUT    /api/v1/rooms/{roomId}/moods/me  오늘 상태값 등록·변경 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/moods  팀 상태값 현황 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "06. Mood", description = "상태값")
public class MoodController {
}
