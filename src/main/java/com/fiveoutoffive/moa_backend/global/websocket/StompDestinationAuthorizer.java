package com.fiveoutoffive.moa_backend.global.websocket;

/**
 * STOMP 구독·전송 경로에 접근할 수 있는지 판단하는 포트.
 * global 은 도메인을 참조하지 않으므로 규칙은 방·채널 도메인의 어댑터가 구현한다.
 */
public interface StompDestinationAuthorizer {

    boolean canAccess(Long userId, String destination);
}
