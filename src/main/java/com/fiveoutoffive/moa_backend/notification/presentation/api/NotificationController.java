package com.fiveoutoffive.moa_backend.notification.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/notifications  알림 목록 [로그인]
//   GET    /api/v1/notifications/unread-count  안읽은 알림 수 [로그인]
//   PATCH  /api/v1/notifications/{notificationId}/read  알림 읽음 처리 [로그인]
//   PATCH  /api/v1/notifications/read-all  알림 모두 읽음 [로그인]
//   DELETE /api/v1/notifications/{notificationId}  알림 삭제 [로그인]
@RestController
@RequiredArgsConstructor
@Tag(name = "14. Notification", description = "알림")
public class NotificationController {
}
