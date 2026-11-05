package com.fiveoutoffive.moa_backend.notice.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/notices/active  노출 중인 공지 (배너) [로그인]
//   GET    /api/v1/notices  공지 목록 [로그인]
//   GET    /api/v1/notices/{noticeId}  공지 상세 [로그인]
//   POST   /api/v1/notices/{noticeId}/dismiss  공지 닫기 [로그인]
@RestController
@RequiredArgsConstructor
@Tag(name = "18. Notice", description = "공지")
public class NoticeController {
}
