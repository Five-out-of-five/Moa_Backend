package com.fiveoutoffive.moa_backend.admin.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/admin/rooms  [관리자] 방 목록 [ADMIN]
//   GET    /api/v1/admin/rooms/{roomId}  [관리자] 방 상세 [ADMIN]
//   POST   /api/v1/admin/rooms/{roomId}/inactivation  [관리자] 방 비활성화 [ADMIN]
//   DELETE /api/v1/admin/rooms/{roomId}/inactivation  [관리자] 방 비활성 해제 [ADMIN]
@RestController
@RequiredArgsConstructor
@Tag(name = "19. Admin", description = "관리자")
public class AdminRoomController {
}
