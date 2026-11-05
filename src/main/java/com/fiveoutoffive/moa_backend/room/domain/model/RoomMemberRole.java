package com.fiveoutoffive.moa_backend.room.domain.model;

/** 방 권한: 방장 / 멤버 (방 관리 기능은 방장만, 방 안에 관리자 권한 없음) (room_member.role) */
public enum RoomMemberRole {
    OWNER,
    MEMBER
}
