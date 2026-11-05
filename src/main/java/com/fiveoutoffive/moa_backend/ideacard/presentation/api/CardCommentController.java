package com.fiveoutoffive.moa_backend.ideacard.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/rooms/{roomId}/idea-cards/{cardId}/comments  댓글 목록 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/idea-cards/{cardId}/comments  댓글 작성 [방 멤버]
//   PATCH  /api/v1/rooms/{roomId}/idea-cards/{cardId}/comments/{commentId}  댓글 수정 [방 멤버]
//   DELETE /api/v1/rooms/{roomId}/idea-cards/{cardId}/comments/{commentId}  댓글 삭제 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "08. IdeaCard", description = "아이디어 카드")
public class CardCommentController {
}
