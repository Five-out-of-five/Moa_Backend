package com.fiveoutoffive.moa_backend.report.domain.model;

/** 접수 / 처리중 / 조치완료 / 반려 (report.status) */
public enum ReportStatus {
    RECEIVED,
    IN_PROGRESS,
    RESOLVED,
    REJECTED
}
