package com.fiveoutoffive.moa_backend.integration.domain.model;

/** 발송 이벤트 (slack_delivery_log.event_type) */
public enum SlackEventType {
    VOTE_CREATED,
    VOTE_CLOSED,
    TASK_DUE_SOON,
    SESSION_SUMMARY
}
