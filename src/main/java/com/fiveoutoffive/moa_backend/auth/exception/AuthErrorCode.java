package com.fiveoutoffive.moa_backend.auth.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    // 재발급 후 재시도
    AUTH_ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_ACCESS_TOKEN_EXPIRED", "액세스 토큰이 만료되었습니다."),
    // 재로그인
    AUTH_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_INVALID", "리프레시 토큰이 만료되었거나 일치하지 않습니다."),
    AUTH_SIGNUP_TICKET_INVALID(HttpStatus.BAD_REQUEST, "AUTH_SIGNUP_TICKET_INVALID", "가입 티켓이 없거나 만료되었습니다."),
    AUTH_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "AUTH_TERMS_NOT_AGREED", "필수 약관에 동의해야 가입할 수 있습니다."),
    AUTH_EMAIL_NOT_PROVIDED(HttpStatus.BAD_REQUEST, "AUTH_EMAIL_NOT_PROVIDED", "소셜 제공자가 이메일을 제공하지 않아 가입할 수 없습니다."),
    AUTH_EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "AUTH_EMAIL_ALREADY_REGISTERED", "이미 다른 소셜 계정으로 가입된 이메일입니다."),
    AUTH_REJOIN_RESTRICTED(HttpStatus.FORBIDDEN, "AUTH_REJOIN_RESTRICTED", "탈퇴 후 30일 동안은 재가입할 수 없습니다."),
    // 정지 종료 시각을 함께 반환
    AUTH_ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "AUTH_ACCOUNT_SUSPENDED", "정지된 계정입니다."),
    AUTH_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "AUTH_PROVIDER_NOT_SUPPORTED", "지원하지 않는 소셜 제공자입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
