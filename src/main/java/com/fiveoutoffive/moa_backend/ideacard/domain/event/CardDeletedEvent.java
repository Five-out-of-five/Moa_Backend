package com.fiveoutoffive.moa_backend.ideacard.domain.event;

/** 카드가 삭제됐다. 관련 충돌 정보·임베딩을 같은 트랜잭션에서 지운다. */
public record CardDeletedEvent(Long roomId, Long cardId) {
}
