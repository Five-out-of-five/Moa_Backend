package com.fiveoutoffive.moa_backend.integration.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/rooms/{roomId}/integrations/notion  노션 연동 조회 [방장]
//   POST   /api/v1/rooms/{roomId}/integrations/notion/authorize  노션 연결 시작 [방장]
//   GET    /api/v1/integrations/notion/callback  노션 OAuth 콜백 [비로그인]
//   GET    /api/v1/rooms/{roomId}/integrations/notion/databases  노션 데이터베이스 목록 [방장]
//   PUT    /api/v1/rooms/{roomId}/integrations/notion  노션 내보내기 설정 [방장]
//   POST   /api/v1/rooms/{roomId}/integrations/notion/exports  노션으로 내보내기 (수동) [방장]
//   GET    /api/v1/rooms/{roomId}/integrations/notion/exports  노션 내보내기 이력 [방장]
//   DELETE /api/v1/rooms/{roomId}/integrations/notion  노션 연동 해제 [방장]
@RestController
@RequiredArgsConstructor
@Tag(name = "16. Integration", description = "슬랙·노션 연동")
public class NotionIntegrationController {
}
