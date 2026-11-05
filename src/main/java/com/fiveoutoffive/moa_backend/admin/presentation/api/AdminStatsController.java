package com.fiveoutoffive.moa_backend.admin.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/admin/stats/overview  [관리자] 서비스 지표 [ADMIN]
//   GET    /api/v1/admin/stats/ai-usage  [관리자] AI 사용량 [ADMIN]
@RestController
@RequiredArgsConstructor
@Tag(name = "19. Admin", description = "관리자")
public class AdminStatsController {
}
