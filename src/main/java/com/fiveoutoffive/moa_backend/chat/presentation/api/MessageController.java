package com.fiveoutoffive.moa_backend.chat.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/rooms/{roomId}/channels/{channelId}/messages  메시지 목록 (커서) [방 멤버]
//   POST   /api/v1/rooms/{roomId}/channels/{channelId}/messages  메시지 전송 [방 멤버]
//   PATCH  /api/v1/rooms/{roomId}/channels/{channelId}/messages/{messageId}  메시지 수정 [방 멤버]
//   DELETE /api/v1/rooms/{roomId}/channels/{channelId}/messages/{messageId}  메시지 삭제 [방 멤버]
//   PUT    /api/v1/rooms/{roomId}/channels/{channelId}/read  읽음 처리 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "13. Chat", description = "채팅")
public class MessageController {
}
