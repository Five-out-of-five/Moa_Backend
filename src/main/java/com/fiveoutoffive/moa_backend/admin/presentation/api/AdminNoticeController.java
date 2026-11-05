package com.fiveoutoffive.moa_backend.admin.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/admin/notices  [관리자] 공지 목록 [ADMIN]
//   POST   /api/v1/admin/notices  [관리자] 공지 등록 [ADMIN]
//   GET    /api/v1/admin/notices/{noticeId}  [관리자] 공지 상세 [ADMIN]
//   PUT    /api/v1/admin/notices/{noticeId}  [관리자] 공지 수정 [ADMIN]
//   DELETE /api/v1/admin/notices/{noticeId}  [관리자] 공지 삭제 [ADMIN]
@RestController
@RequiredArgsConstructor
@Tag(name = "19. Admin", description = "관리자")
public class AdminNoticeController {
}
