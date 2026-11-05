package com.fiveoutoffive.moa_backend.notice.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum NoticeErrorCode implements BaseErrorCode {

    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE_NOT_FOUND", "공지를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
