package com.fiveoutoffive.moa_backend.account.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/accounts/me  내 계정 조회 [로그인]
//   PATCH  /api/v1/accounts/me  기본 프로필(이름) 수정 [로그인]
//   PATCH  /api/v1/accounts/me/ai-settings  AI 설정 변경 (톤앤매너 강도) [로그인]
//   PUT    /api/v1/accounts/me/last-room  마지막으로 머문 방 저장 [로그인]
//   GET    /api/v1/accounts/me/withdrawal-check  탈퇴 가능 여부 확인 [로그인]
//   DELETE /api/v1/accounts/me  회원 탈퇴 [로그인]
@RestController
@RequiredArgsConstructor
@Tag(name = "02. Account", description = "계정")
public class AccountController {
}
