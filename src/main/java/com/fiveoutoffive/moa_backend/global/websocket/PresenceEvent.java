package com.fiveoutoffive.moa_backend.global.websocket;

/** 접속 상태 변화 (/topic/rooms/{roomId}/presence) */
public record PresenceEvent(Long accountId, boolean online) {
}
