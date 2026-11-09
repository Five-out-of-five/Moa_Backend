package com.fiveoutoffive.moa_backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements BaseErrorCode {
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_INTERNAL_ERROR", "서버 내부에서 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_INVALID_INPUT", "잘못된 요청입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다."),
    API_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_NOT_FOUND", "요청하신 API 경로를 찾을 수 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON_FORBIDDEN", "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_UNAUTHORIZED", "로그인이 필요한 서비스입니다. 인증 토큰을 확인해 주세요."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_FILE_UPLOAD_FAILED", "파일 업로드에 실패했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "FILE_TYPE_NOT_ALLOWED", "허용되지 않는 파일 형식입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_ACCESS_TOKEN_EXPIRED", "토큰이 만료되었습니다. 다시 로그인해주세요."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "COMMON_UNAUTHORIZED", "유효하지 않은 토큰입니다."),
    UPLOAD_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_SIZE_EXCEEDED", "파일 크기 제한을 넘었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
