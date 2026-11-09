package com.fiveoutoffive.moa_backend.ai.application.command;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;

public record SummaryToCardCommand(
        CardType cardType,
        String title
) {
}
