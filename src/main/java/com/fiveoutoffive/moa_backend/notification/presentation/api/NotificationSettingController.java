package com.fiveoutoffive.moa_backend.notification.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/notification-settings  전역 알림 설정 조회 [로그인]
//   PUT    /api/v1/notification-settings  전역 알림 설정 변경 [로그인]
//   GET    /api/v1/rooms/{roomId}/notification-settings  방별 알림 설정 조회 [방 멤버]
//   PUT    /api/v1/rooms/{roomId}/notification-settings  방별 알림 설정 변경 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "14. Notification", description = "알림")
public class NotificationSettingController {
}
