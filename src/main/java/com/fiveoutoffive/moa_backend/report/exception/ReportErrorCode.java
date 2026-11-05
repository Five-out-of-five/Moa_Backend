package com.fiveoutoffive.moa_backend.report.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum ReportErrorCode implements BaseErrorCode {

    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", "신고를 찾을 수 없습니다."),
    REPORT_DUPLICATED(HttpStatus.CONFLICT, "REPORT_DUPLICATED", "이미 신고한 대상입니다."),
    REPORT_OWN_CONTENT(HttpStatus.BAD_REQUEST, "REPORT_OWN_CONTENT", "본인이 작성한 내용은 신고할 수 없습니다."),
    REPORT_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_TARGET_NOT_FOUND", "신고 대상이 없거나 이미 삭제되었습니다."),
    REPORT_BLOCKED(HttpStatus.FORBIDDEN, "REPORT_BLOCKED", "허위 신고 반복으로 신고 기능이 제한되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
