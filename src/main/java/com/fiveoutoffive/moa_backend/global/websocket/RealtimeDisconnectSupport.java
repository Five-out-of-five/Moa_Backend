package com.fiveoutoffive.moa_backend.global.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;

/** 로그아웃·정지 시 사용자의 STOMP 세션을 서버에서 닫는다. */
final class RealtimeDisconnectSupport {

    private RealtimeDisconnectSupport() {
    }

    static Message<byte[]> disconnectMessage(String sessionId) {
        org.springframework.messaging.simp.SimpMessageHeaderAccessor accessor =
                org.springframework.messaging.simp.SimpMessageHeaderAccessor.create(SimpMessageType.DISCONNECT_ACK);
        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);
        return org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
