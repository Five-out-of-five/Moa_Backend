package com.fiveoutoffive.moa_backend.room.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/rooms/{roomId}/join-requests  참여 요청 목록 [방장]
//   POST   /api/v1/rooms/{roomId}/join-requests/{requestId}/approve  참여 요청 승인 [방장]
//   POST   /api/v1/rooms/{roomId}/join-requests/{requestId}/reject  참여 요청 거절 [방장]
@RestController
@RequiredArgsConstructor
@Tag(name = "04. Room", description = "방")
public class JoinRequestController {
}
