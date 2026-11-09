package com.fiveoutoffive.moa_backend.ideacard.application.command;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;

public record CardMoveCommand(
        CardType cardType,
        Integer toIndex,
        Long version
) {
}
