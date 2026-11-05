package com.fiveoutoffive.moa_backend.integration.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum IntegrationErrorCode implements BaseErrorCode {

    INTEGRATION_NOT_FOUND(HttpStatus.NOT_FOUND, "INTEGRATION_NOT_FOUND", "연동 설정이 없습니다."),
    INTEGRATION_SLACK_URL_INVALID(HttpStatus.BAD_REQUEST, "INTEGRATION_SLACK_URL_INVALID", "슬랙 Webhook URL 형식이 올바르지 않습니다."),
    INTEGRATION_SLACK_TEST_FAILED(HttpStatus.BAD_GATEWAY, "INTEGRATION_SLACK_TEST_FAILED", "슬랙 테스트 발송에 실패했습니다."),
    // state 불일치·만료
    INTEGRATION_NOTION_STATE_INVALID(HttpStatus.BAD_REQUEST, "INTEGRATION_NOTION_STATE_INVALID", "노션 연결 요청이 유효하지 않습니다."),
    // 재연결 필요
    INTEGRATION_NOTION_AUTH_EXPIRED(HttpStatus.CONFLICT, "INTEGRATION_NOTION_AUTH_EXPIRED", "노션 연결 권한이 만료되었습니다."),
    INTEGRATION_NOTION_DATABASE_NOT_FOUND(HttpStatus.CONFLICT, "INTEGRATION_NOTION_DATABASE_NOT_FOUND", "대상 노션 데이터베이스를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
