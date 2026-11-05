package com.fiveoutoffive.moa_backend.session.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   POST   /api/v1/rooms/{roomId}/sessions  회차 시작 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/sessions/current  진행 중 회차 조회 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/sessions/{sessionId}/close  회차 수동 마감 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/sessions  지난 회차 목록 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/sessions/{sessionId}  회차 상세 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "07. Session", description = "회차")
public class SessionController {
}
