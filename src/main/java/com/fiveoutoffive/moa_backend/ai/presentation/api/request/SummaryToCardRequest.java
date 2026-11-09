package com.fiveoutoffive.moa_backend.ai.presentation.api.request;

import com.fiveoutoffive.moa_backend.ai.application.command.SummaryToCardCommand;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SummaryToCardRequest(
        @NotNull CardType cardType,
        @NotBlank @Size(max=100) String title
) {

    public SummaryToCardCommand toCommand() {
        return new SummaryToCardCommand(cardType, title);
    }
}
