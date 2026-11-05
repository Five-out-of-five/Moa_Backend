package com.fiveoutoffive.moa_backend.room.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   POST   /api/v1/rooms  방 개설 [로그인]
//   GET    /api/v1/rooms  내 방 목록 [로그인]
//   GET    /api/v1/rooms/{roomId}  방 정보 조회 (방 진입) [방 멤버]
//   PATCH  /api/v1/rooms/{roomId}  방 설정 변경 [방장]
//   DELETE /api/v1/rooms/{roomId}  방 삭제 [방장]
@RestController
@RequiredArgsConstructor
@Tag(name = "04. Room", description = "방")
public class RoomController {
}
