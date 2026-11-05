package com.fiveoutoffive.moa_backend.admin.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/admin/accounts  [관리자] 회원 목록 [ADMIN]
//   GET    /api/v1/admin/accounts/{accountId}  [관리자] 회원 상세 [ADMIN]
//   POST   /api/v1/admin/accounts/{accountId}/suspension  [관리자] 회원 정지 [ADMIN]
//   DELETE /api/v1/admin/accounts/{accountId}/suspension  [관리자] 회원 정지 해제 [ADMIN]
//   PUT    /api/v1/admin/accounts/{accountId}/role  [관리자] 서비스 권한 변경 [ADMIN]
@RestController
@RequiredArgsConstructor
@Tag(name = "19. Admin", description = "관리자")
public class AdminAccountController {
}
