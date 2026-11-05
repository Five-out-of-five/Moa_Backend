package com.fiveoutoffive.moa_backend.task.domain.model;

/** 이력 종류 (UNASSIGNED = 담당자가 방을 나가 미담당 전환) (task_history.history_type) */
public enum TaskHistoryType {
    CREATED,
    STATUS_CHANGED,
    ASSIGNED,
    REJECTED,
    UNASSIGNED
}
