package com.fiveoutoffive.moa_backend.task.domain.model;

/** 대기 / 진행 / 검토 / 완료 (반려는 상태가 아닌 담당자 반환 동작) (task.status) */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    REVIEW,
    DONE
}
