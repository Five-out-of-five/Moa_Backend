package com.fiveoutoffive.moa_backend.briefing.domain.model;

/** 이벤트 종류 (board_event.event_type) */
public enum BoardEventType {
    CARD_CREATED,
    CARD_COMMENTED,
    CARD_CONVERTED,
    TASK_CREATED,
    TASK_ASSIGNED,
    TASK_REJECTED,
    TASK_COMPLETED,
    VOTE_CREATED,
    VOTE_CLOSED,
    SESSION_CLOSED,
    MEMBER_JOINED
}
