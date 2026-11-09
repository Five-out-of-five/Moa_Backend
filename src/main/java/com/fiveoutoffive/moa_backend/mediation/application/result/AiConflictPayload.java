package com.fiveoutoffive.moa_backend.mediation.application.result;

import java.util.List;

public record AiConflictPayload(List<Item> conflicts) {

    public record Item(Long cardId, String reason) {
    }
}
