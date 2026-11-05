package com.fiveoutoffive.moa_backend.admin.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum AdminErrorCode implements BaseErrorCode {

    ADMIN_CANNOT_SUSPEND_ADMIN(HttpStatus.CONFLICT, "ADMIN_CANNOT_SUSPEND_ADMIN", "관리자 계정은 정지할 수 없습니다."),
    ADMIN_ACCOUNT_WITHDRAWN(HttpStatus.CONFLICT, "ADMIN_ACCOUNT_WITHDRAWN", "탈퇴한 회원은 상태를 변경할 수 없습니다."),
    ADMIN_ALREADY_SUSPENDED(HttpStatus.CONFLICT, "ADMIN_ALREADY_SUSPENDED", "이미 정지된 회원입니다."),
    ADMIN_NOT_SUSPENDED(HttpStatus.CONFLICT, "ADMIN_NOT_SUSPENDED", "정지 상태가 아닙니다."),
    ADMIN_CANNOT_CHANGE_OWN_ROLE(HttpStatus.CONFLICT, "ADMIN_CANNOT_CHANGE_OWN_ROLE", "본인 관리자 권한은 스스로 바꿀 수 없습니다."),
    ADMIN_LAST_ADMIN(HttpStatus.CONFLICT, "ADMIN_LAST_ADMIN", "관리자는 최소 1명 이상 유지해야 합니다."),
    ADMIN_ROOM_ALREADY_INACTIVE(HttpStatus.CONFLICT, "ADMIN_ROOM_ALREADY_INACTIVE", "이미 비활성화된 방입니다."),
    ADMIN_ROOM_NOT_INACTIVE(HttpStatus.CONFLICT, "ADMIN_ROOM_NOT_INACTIVE", "비활성 상태가 아닌 방입니다."),
    ADMIN_REPORT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "ADMIN_REPORT_ALREADY_PROCESSED", "이미 처리된 신고입니다."),
    // 동시 1건
    ADMIN_NOTICE_BANNER_CONFLICT(HttpStatus.CONFLICT, "ADMIN_NOTICE_BANNER_CONFLICT", "같은 기간에 노출 중인 긴급 배너가 있습니다."),
    ADMIN_NOTICE_PERIOD_INVALID(HttpStatus.BAD_REQUEST, "ADMIN_NOTICE_PERIOD_INVALID", "노출 종료가 노출 시작보다 늦어야 합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
