package com.fiveoutoffive.moa_backend.task.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum TaskErrorCode implements BaseErrorCode {

    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "TASK_NOT_FOUND", "업무를 찾을 수 없습니다."),
    // API 설명의 허용 대상 참고
    TASK_NO_PERMISSION(HttpStatus.FORBIDDEN, "TASK_NO_PERMISSION", "이 업무에 대한 권한이 없습니다."),
    TASK_ASSIGNEE_NOT_MEMBER(HttpStatus.BAD_REQUEST, "TASK_ASSIGNEE_NOT_MEMBER", "방 멤버만 담당자로 지정할 수 있습니다."),
    TASK_DUE_DATE_IN_PAST(HttpStatus.BAD_REQUEST, "TASK_DUE_DATE_IN_PAST", "마감일을 과거로 지정할 수 없습니다."),
    // 최신 상태를 함께 반환
    TASK_VERSION_CONFLICT(HttpStatus.CONFLICT, "TASK_VERSION_CONFLICT", "다른 멤버가 먼저 변경했습니다."),
    TASK_NOT_ASSIGNEE(HttpStatus.FORBIDDEN, "TASK_NOT_ASSIGNEE", "담당자만 반려할 수 있습니다."),
    TASK_CANNOT_REJECT_OWN(HttpStatus.CONFLICT, "TASK_CANNOT_REJECT_OWN", "등록자와 담당자가 같은 업무는 반려할 수 없습니다."),
    TASK_SOURCE_INVALID(HttpStatus.BAD_REQUEST, "TASK_SOURCE_INVALID", "출처는 카드와 투표 중 하나만 지정할 수 있습니다."),
    SPRINT_NOT_FOUND(HttpStatus.NOT_FOUND, "SPRINT_NOT_FOUND", "스프린트를 찾을 수 없습니다."),
    SPRINT_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "SPRINT_ALREADY_IN_PROGRESS", "진행 중인 스프린트가 이미 있습니다."),
    SPRINT_ALREADY_CLOSED(HttpStatus.CONFLICT, "SPRINT_ALREADY_CLOSED", "이미 마감된 스프린트입니다."),
    SPRINT_NOT_IN_PROGRESS(HttpStatus.CONFLICT, "SPRINT_NOT_IN_PROGRESS", "진행 중인 스프린트로만 업무를 옮길 수 있습니다."),
    SPRINT_PERIOD_INVALID(HttpStatus.BAD_REQUEST, "SPRINT_PERIOD_INVALID", "종료일은 시작일보다 앞설 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
