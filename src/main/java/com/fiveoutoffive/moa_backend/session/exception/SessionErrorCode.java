package com.fiveoutoffive.moa_backend.session.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum SessionErrorCode implements BaseErrorCode {

    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "회차를 찾을 수 없습니다."),
    SESSION_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "SESSION_ALREADY_IN_PROGRESS", "진행 중인 회차가 이미 있습니다."),
    // '회차 시작' 안내
    SESSION_NONE_IN_PROGRESS(HttpStatus.CONFLICT, "SESSION_NONE_IN_PROGRESS", "진행 중인 회차가 없습니다."),
    SESSION_ALREADY_CLOSED(HttpStatus.CONFLICT, "SESSION_ALREADY_CLOSED", "이미 마감된 회차입니다."),
    SESSION_DEADLINE_IN_PAST(HttpStatus.BAD_REQUEST, "SESSION_DEADLINE_IN_PAST", "마감 시각을 과거로 설정할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
