package com.fiveoutoffive.moa_backend.room.domain.model;

/** 대기 / 승인 / 거절 / 만료 (room_join_request.status) */
public enum JoinRequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
    EXPIRED
}
