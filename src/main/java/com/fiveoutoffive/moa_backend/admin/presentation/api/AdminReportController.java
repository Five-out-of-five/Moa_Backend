package com.fiveoutoffive.moa_backend.admin.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/admin/reports  [관리자] 신고 목록 [ADMIN]
//   GET    /api/v1/admin/reports/{reportId}  [관리자] 신고 상세 [ADMIN]
//   POST   /api/v1/admin/reports/{reportId}/review  [관리자] 신고 처리 시작 [ADMIN]
//   POST   /api/v1/admin/reports/{reportId}/resolve  [관리자] 신고 조치 완료 [ADMIN]
//   POST   /api/v1/admin/reports/{reportId}/reject  [관리자] 신고 반려 [ADMIN]
@RestController
@RequiredArgsConstructor
@Tag(name = "19. Admin", description = "관리자")
public class AdminReportController {
}
