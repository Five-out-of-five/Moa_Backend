package com.fiveoutoffive.moa_backend.global.websocket;

import java.util.List;

/**
 * 접속 상태 변화를 어떤 방에 알릴지 결정하는 포트.
 * 접속 상태는 계정 단위라 참여 중인 모든 방에 같은 값을 보낸다. 구현은 방 도메인이 담당한다.
 */
public interface PresenceSubscriberPort {

    /** @return 사용자가 ACTIVE 멤버로 참여 중인 방 ID 목록. 없으면 빈 리스트 (null 반환 금지) */
    List<Long> findRoomIds(Long userId);
}
