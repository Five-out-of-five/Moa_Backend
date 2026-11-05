package com.fiveoutoffive.moa_backend.room.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/rooms/{roomId}/invite-link  초대 링크 조회 [방장]
//   GET    /api/v1/invites/{inviteCode}  초대 링크로 방 정보 확인 [로그인]
//   POST   /api/v1/invites/{inviteCode}/join-requests  참여 요청 [로그인]
@RestController
@RequiredArgsConstructor
@Tag(name = "04. Room", description = "방")
public class RoomInviteController {
}
