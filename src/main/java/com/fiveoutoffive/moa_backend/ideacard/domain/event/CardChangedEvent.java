package com.fiveoutoffive.moa_backend.ideacard.domain.event;

/** 카드가 등록·수정됐다. 임베딩 갱신과 충돌 분석이 커밋 후 구독한다. */
public record CardChangedEvent(Long roomId, Long cardId, boolean created) {
}
