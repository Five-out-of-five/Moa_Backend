package com.fiveoutoffive.moa_backend.global.type;

/**
 * 응답의 {@code MemberRef.memberStatus} 값.
 * 방 멤버 상태(room_member.status)와 계정 탈퇴(account.status = WITHDRAWN)를 합쳐 하나로 내려준다.
 * 탈퇴가 방 멤버 상태보다 우선한다.
 */
public enum MemberStatus {
    ACTIVE,
    LEFT,
    KICKED,
    WITHDRAWN
}
