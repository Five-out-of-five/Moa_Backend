package com.fiveoutoffive.moa_backend.chat.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/rooms/{roomId}/channels  채널 목록 (사이드바) [방 멤버]
//   POST   /api/v1/rooms/{roomId}/channels  채널 생성 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/channels/{channelId}  채널 상세 [방 멤버]
//   PATCH  /api/v1/rooms/{roomId}/channels/{channelId}  채널 수정 [방 멤버]
//   DELETE /api/v1/rooms/{roomId}/channels/{channelId}  채널 삭제 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/channels/{channelId}/join  공개 채널 참여 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/channels/{channelId}/leave  채널 나가기 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/channels/{channelId}/accept-invitation  채널 초대 수락 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/channels/{channelId}/members  채널 참여자 목록 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/channels/{channelId}/members  채널 멤버 추가 [방 멤버]
//   DELETE /api/v1/rooms/{roomId}/channels/{channelId}/members/{accountId}  채널 멤버 내보내기 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/direct-channels  1:1 대화 시작 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/channels/{channelId}/hide  1:1 대화 숨기기 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "13. Chat", description = "채팅")
public class ChannelController {
}
