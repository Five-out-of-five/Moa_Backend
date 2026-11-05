package com.fiveoutoffive.moa_backend.dashboard.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum DashboardErrorCode implements BaseErrorCode {

    DASHBOARD_WIDGET_NOT_FOUND(HttpStatus.NOT_FOUND, "DASHBOARD_WIDGET_NOT_FOUND", "위젯을 찾을 수 없습니다."),
    DASHBOARD_WIDGET_ALREADY_EXISTS(HttpStatus.CONFLICT, "DASHBOARD_WIDGET_ALREADY_EXISTS", "이미 추가된 위젯입니다."),
    DASHBOARD_WIDGET_TOO_SMALL(HttpStatus.BAD_REQUEST, "DASHBOARD_WIDGET_TOO_SMALL", "위젯 최소 크기보다 작습니다."),
    // 가로 12칸, 위젯 최대 12×8
    DASHBOARD_WIDGET_OUT_OF_GRID(HttpStatus.BAD_REQUEST, "DASHBOARD_WIDGET_OUT_OF_GRID", "그리드 범위를 벗어났습니다."),
    DASHBOARD_WIDGET_OVERLAP(HttpStatus.CONFLICT, "DASHBOARD_WIDGET_OVERLAP", "다른 위젯과 겹치는 위치입니다."),
    DASHBOARD_LAST_WIDGET(HttpStatus.CONFLICT, "DASHBOARD_LAST_WIDGET", "최소 1개의 위젯은 유지해야 합니다."),
    DASHBOARD_UNDO_NOT_AVAILABLE(HttpStatus.CONFLICT, "DASHBOARD_UNDO_NOT_AVAILABLE", "되돌릴 AI 구성이 없거나 이후 배치가 변경되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
