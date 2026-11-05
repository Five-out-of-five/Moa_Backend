package com.fiveoutoffive.moa_backend.room.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/rooms/{roomId}/members  멤버 목록 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/members/{accountId}  멤버 프로필 조회 [방 멤버]
//   PATCH  /api/v1/rooms/{roomId}/members/me/profile  방별 프로필 설정 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/members/me/leave-check  방 나가기 확인 [방 멤버]
//   DELETE /api/v1/rooms/{roomId}/members/me  방 나가기 [방 멤버]
//   DELETE /api/v1/rooms/{roomId}/members/{accountId}  멤버 내보내기 [방장]
//   POST   /api/v1/rooms/{roomId}/delegate-owner  방장 위임 [방장]
@RestController
@RequiredArgsConstructor
@Tag(name = "04. Room", description = "방")
public class RoomMemberController {
}
