package com.fiveoutoffive.moa_backend.ideacard.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   PUT    /api/v1/rooms/{roomId}/idea-cards/{cardId}/reactions/{emoji}  리액션 추가 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/idea-cards/{cardId}/reactions  리액션한 사용자 목록 [방 멤버]
//   DELETE /api/v1/rooms/{roomId}/idea-cards/{cardId}/reactions/{emoji}  리액션 취소 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "08. IdeaCard", description = "아이디어 카드")
public class CardReactionController {
}
