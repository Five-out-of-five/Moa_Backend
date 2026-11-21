package com.fiveoutoffive.moa_backend.mediation.application.result;

import com.fiveoutoffive.moa_backend.mediation.domain.model.ConflictStatus;
import java.time.LocalDateTime;

/** adoptedCardId: 이 충돌에서 채택된 절충안 카드 ID. 채택 전이면 null (채택해도 status 는 PROPOSED 로 남는다). */
public record ConflictSummaryResponse(Long conflictId, ConflictStatus status, CardRef cardA, CardRef cardB, LocalDateTime detectedAt, Long adoptedCardId) {

    public record CardRef(Long cardId, String title) {
    }
}
