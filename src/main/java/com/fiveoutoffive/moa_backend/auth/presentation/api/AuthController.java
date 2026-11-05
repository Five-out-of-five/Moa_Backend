package com.fiveoutoffive.moa_backend.auth.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/auth/signup-tickets/{ticket}  가입 화면 정보 조회 [비로그인]
//   POST   /api/v1/auth/signup  최초 가입 완료 (약관 동의) [비로그인]
//   POST   /api/v1/auth/refresh  토큰 재발급 [비로그인]
//   POST   /api/v1/auth/logout  로그아웃 [비로그인]
@RestController
@RequiredArgsConstructor
@Tag(name = "01. Auth", description = "인증")
public class AuthController {
}
