package com.fiveoutoffive.moa_backend.ai.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   POST   /api/v1/rooms/{roomId}/ai/tone-checks  톤앤매너 점검 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "15. AI", description = "AI")
public class ToneCheckController {
}
