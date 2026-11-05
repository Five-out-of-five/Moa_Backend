package com.fiveoutoffive.moa_backend.mediation.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum MediationErrorCode implements BaseErrorCode {

    MEDIATION_WIDGET_REQUIRED(HttpStatus.CONFLICT, "MEDIATION_WIDGET_REQUIRED", "중재 위젯이 있는 방에서만 사용할 수 있습니다."),
    CONFLICT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONFLICT_NOT_FOUND", "충돌 정보를 찾을 수 없습니다."),
    CONFLICT_ALREADY_ADOPTED(HttpStatus.CONFLICT, "CONFLICT_ALREADY_ADOPTED", "이미 절충안이 채택된 충돌입니다."),
    CONFLICT_ALREADY_DISMISSED(HttpStatus.CONFLICT, "CONFLICT_ALREADY_DISMISSED", "이미 해제된 충돌입니다."),
    // 요청자 본인만 조회 가능
    DRAFT_NOT_FOUND(HttpStatus.NOT_FOUND, "DRAFT_NOT_FOUND", "절충안 초안을 찾을 수 없습니다."),
    DRAFT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "DRAFT_ALREADY_PROCESSED", "이미 채택되었거나 폐기된 초안입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
