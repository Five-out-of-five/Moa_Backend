package com.fiveoutoffive.moa_backend.dashboard.domain.event;

/** 중재 위젯이 없던 방에 생겼다. 커밋 후 진행 중 회차의 기존 카드를 충돌 분석한다. */
public record MediationWidgetAddedEvent(Long roomId) {
}
