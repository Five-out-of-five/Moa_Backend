package com.fiveoutoffive.moa_backend.room.domain.event;

/** 참여 요청이 승인되어 멤버가 합류했다. */
public record RoomMemberJoinedEvent(Long roomId, Long accountId) {
}
