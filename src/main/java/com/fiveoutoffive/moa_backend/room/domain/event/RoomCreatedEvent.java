package com.fiveoutoffive.moa_backend.room.domain.event;

/** 방이 개설됐다. 기본 위젯·기본 채널을 같은 트랜잭션에서 만든다. */
public record RoomCreatedEvent(Long roomId, Long ownerAccountId) {
}
