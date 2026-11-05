package com.fiveoutoffive.moa_backend.briefing.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum BriefingErrorCode implements BaseErrorCode {

    // API 명세에 이 도메인 전용 에러 코드가 아직 없다. 필요하면 추가한다.
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
