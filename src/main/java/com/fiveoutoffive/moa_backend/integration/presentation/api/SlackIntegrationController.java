package com.fiveoutoffive.moa_backend.integration.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/rooms/{roomId}/integrations/slack  슬랙 연동 조회 [방장]
//   POST   /api/v1/rooms/{roomId}/integrations/slack/test  슬랙 테스트 발송 [방장]
//   PUT    /api/v1/rooms/{roomId}/integrations/slack  슬랙 연동 등록·수정 [방장]
//   DELETE /api/v1/rooms/{roomId}/integrations/slack  슬랙 연동 해제 [방장]
//   GET    /api/v1/rooms/{roomId}/integrations/slack/deliveries  슬랙 발송 이력 [방장]
@RestController
@RequiredArgsConstructor
@Tag(name = "16. Integration", description = "슬랙·노션 연동")
public class SlackIntegrationController {
}
