package com.fiveoutoffive.moa_backend.auth.presentation.api;

import com.fiveoutoffive.moa_backend.auth.application.result.IssuedTokens;
import com.fiveoutoffive.moa_backend.auth.application.result.SignupTicketResponse;
import com.fiveoutoffive.moa_backend.auth.application.result.TokenResponse;
import com.fiveoutoffive.moa_backend.auth.application.usecase.AuthUseCase;
import com.fiveoutoffive.moa_backend.auth.presentation.api.request.SignupRequest;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.exception.GlobalErrorCode;
import com.fiveoutoffive.moa_backend.global.security.GlobalJwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//   GET  /api/v1/auth/signup-tickets/{ticket}, POST /api/v1/auth/signup, /refresh, /logout
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "01. Auth", description = "인증")
public class AuthController {

    private final AuthUseCase authUseCase;
    private final GlobalJwtProvider jwtProvider;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @GetMapping("/signup-tickets/{ticket}")
    @Operation(summary = "가입 화면 정보 조회")
    public ApiResponse<SignupTicketResponse> getSignupTicket(@PathVariable String ticket) {
        return ApiResponse.success(authUseCase.getSignupTicket(ticket));
    }

    @PostMapping("/signup")
    @Operation(summary = "최초 가입 완료 (약관 동의)")
    public ApiResponse<TokenResponse> signup(@Valid @RequestBody SignupRequest request, HttpServletResponse response) {
        IssuedTokens issued = authUseCase.signup(request.toCommand());
        response.addHeader(HttpHeaders.SET_COOKIE, jwtProvider.createRefreshCookie(issued.refreshToken()).toString());
        return ApiResponse.success(issued.token());
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급")
    public ApiResponse<TokenResponse> refresh(@CookieValue(value = GlobalJwtProvider.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
                                              @RequestHeader(value = HttpHeaders.ORIGIN, required = false) String origin,
                                              HttpServletResponse response) {
        verifyOrigin(origin);
        IssuedTokens issued = authUseCase.refresh(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, jwtProvider.createRefreshCookie(issued.refreshToken()).toString());
        return ApiResponse.success(issued.token());
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    public ApiResponse<Void> logout(@CookieValue(value = GlobalJwtProvider.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
                                    @RequestHeader(value = HttpHeaders.ORIGIN, required = false) String origin,
                                    HttpServletResponse response) {
        verifyOrigin(origin);
        authUseCase.logout(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, jwtProvider.deleteRefreshCookie().toString());
        return ApiResponse.empty();
    }

    /** 쿠키로 동작하는 API 의 CSRF 방어. 브라우저가 보낸 Origin 이 허용 목록에 있어야 한다. */
    private void verifyOrigin(String origin) {
        if (origin != null && Arrays.stream(allowedOrigins.split(",")).map(String::trim).noneMatch(origin::equals)) {
            throw new BusinessException(GlobalErrorCode.ACCESS_DENIED);
        }
    }
}
