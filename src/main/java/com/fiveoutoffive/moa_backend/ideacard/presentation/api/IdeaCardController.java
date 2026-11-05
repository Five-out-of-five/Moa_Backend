package com.fiveoutoffive.moa_backend.ideacard.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/rooms/{roomId}/idea-cards  아이디어 보드 카드 목록 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/idea-cards  아이디어 카드 등록 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/idea-cards/{cardId}  아이디어 카드 상세 [방 멤버]
//   PATCH  /api/v1/rooms/{roomId}/idea-cards/{cardId}  아이디어 카드 수정 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/idea-cards/{cardId}/edit-lock  카드 편집 잠금 [방 멤버]
//   DELETE /api/v1/rooms/{roomId}/idea-cards/{cardId}/edit-lock  카드 편집 잠금 해제 [방 멤버]
//   DELETE /api/v1/rooms/{roomId}/idea-cards/{cardId}  아이디어 카드 삭제 [방 멤버]
//   PATCH  /api/v1/rooms/{roomId}/idea-cards/{cardId}/position  카드 이동·정렬 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/idea-cards/{cardId}/files  카드 첨부 추가 [방 멤버]
//   DELETE /api/v1/rooms/{roomId}/idea-cards/{cardId}/files/{fileId}  카드 첨부 삭제 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "08. IdeaCard", description = "아이디어 카드")
public class IdeaCardController {
}
