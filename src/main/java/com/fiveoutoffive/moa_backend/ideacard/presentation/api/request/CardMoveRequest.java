package com.fiveoutoffive.moa_backend.ideacard.presentation.api.request;

import com.fiveoutoffive.moa_backend.ideacard.application.command.CardMoveCommand;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CardMoveRequest(
        @NotNull CardType cardType,
        @NotNull @Min(0) Integer toIndex,
        @NotNull Long version
) {

    public CardMoveCommand toCommand() {
        return new CardMoveCommand(cardType, toIndex, version);
    }
}
