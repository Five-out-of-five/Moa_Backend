package com.fiveoutoffive.moa_backend.account.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum AccountErrorCode implements BaseErrorCode {

    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "회원을 찾을 수 없습니다."),
    ACCOUNT_OWNED_ROOM_EXISTS(HttpStatus.CONFLICT, "ACCOUNT_OWNED_ROOM_EXISTS", "방장으로 있는 방이 있어 탈퇴할 수 없습니다."),
    ACCOUNT_WITHDRAW_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "ACCOUNT_WITHDRAW_CONFIRM_MISMATCH", "탈퇴 확인 문구가 일치하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
