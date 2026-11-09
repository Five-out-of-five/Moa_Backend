package com.fiveoutoffive.moa_backend.vote.domain.event;

/** 슬랙 발송이 구독한다. */
public record VoteLifecycleEvent(Long roomId, Long voteId, String title, boolean closed) {
}
