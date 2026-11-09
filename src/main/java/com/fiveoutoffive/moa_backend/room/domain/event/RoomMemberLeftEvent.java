package com.fiveoutoffive.moa_backend.room.domain.event;

import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberStatus;

/** 멤버가 방을 나가거나 내보내지거나 탈퇴했다. 업무·채널·알림 설정·브리핑이 같은 트랜잭션에서 정리한다. */
public record RoomMemberLeftEvent(Long roomId, Long accountId, RoomMemberStatus status) {
}
