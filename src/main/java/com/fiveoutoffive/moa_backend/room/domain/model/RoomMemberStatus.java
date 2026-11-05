package com.fiveoutoffive.moa_backend.room.domain.model;

/** 참여 상태 (KICKED 는 재참여 불가) (room_member.status) */
public enum RoomMemberStatus {
    ACTIVE,
    LEFT,
    KICKED
}
