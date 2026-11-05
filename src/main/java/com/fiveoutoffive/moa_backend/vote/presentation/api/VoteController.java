package com.fiveoutoffive.moa_backend.vote.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/rooms/{roomId}/votes  투표 목록 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/votes  투표 생성 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/votes/{voteId}  투표 상세 [방 멤버]
//   PATCH  /api/v1/rooms/{roomId}/votes/{voteId}  투표 수정 [방 멤버]
//   DELETE /api/v1/rooms/{roomId}/votes/{voteId}  투표 삭제 [방 멤버]
//   PUT    /api/v1/rooms/{roomId}/votes/{voteId}/ballots/me  투표 참여·선택 변경 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/votes/{voteId}/close  투표 수동 마감 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/votes/{voteId}/revote  재투표 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "10. Vote", description = "투표")
public class VoteController {
}
