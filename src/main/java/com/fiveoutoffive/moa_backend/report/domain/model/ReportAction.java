package com.fiveoutoffive.moa_backend.report.domain.model;

/** 조치: 콘텐츠 삭제 / 계정 정지 / 경고 / 조치 없음 (report.action) */
public enum ReportAction {
    DELETE_CONTENT,
    SUSPEND_ACCOUNT,
    WARN,
    NONE
}
