package com.fiveoutoffive.moa_backend.mediation.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/rooms/{roomId}/conflicts  충돌 목록 (중재 위젯) [방 멤버]
//   GET    /api/v1/rooms/{roomId}/conflicts/{conflictId}  충돌 상세 (두 카드 비교) [방 멤버]
//   POST   /api/v1/rooms/{roomId}/conflicts/{conflictId}/dismiss  충돌 배지 해제 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/conflicts/{conflictId}/compromise-drafts  절충안 생성 (AI) [방 멤버]
//   POST   /api/v1/rooms/{roomId}/conflicts/{conflictId}/compromise-drafts/{draftId}/adopt  절충안 채택 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/conflicts/{conflictId}/compromise-drafts/{draftId}/discard  절충안 폐기 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "12. Mediation", description = "중재")
public class ConflictController {
}
