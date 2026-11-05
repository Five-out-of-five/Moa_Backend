package com.fiveoutoffive.moa_backend.ai.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum AiErrorCode implements BaseErrorCode {

    AI_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AI_QUOTA_EXCEEDED", "오늘 이 방의 AI 호출 한도를 모두 사용했습니다."),
    // 재시도 안내
    AI_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI_UNAVAILABLE", "AI 응답에 실패했거나 지연되었습니다."),
    AI_NOT_ENOUGH_CARDS(HttpStatus.CONFLICT, "AI_NOT_ENOUGH_CARDS", "카드가 3건 미만이라 요약할 수 없습니다."),
    AI_SUMMARY_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_SUMMARY_NOT_FOUND", "요약을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
