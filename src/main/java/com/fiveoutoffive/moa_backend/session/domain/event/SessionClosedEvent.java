package com.fiveoutoffive.moa_backend.session.domain.event;

/** 회차가 마감됐다. 충돌 일괄 분석, 노션 자동 내보내기, 슬랙 발송이 구독한다. */
public record SessionClosedEvent(Long roomId, Long sessionId, String title) {
}
