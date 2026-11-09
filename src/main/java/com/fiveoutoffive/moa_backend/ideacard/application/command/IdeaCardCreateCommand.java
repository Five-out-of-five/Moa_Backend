package com.fiveoutoffive.moa_backend.ideacard.application.command;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;
import java.util.List;

public record IdeaCardCreateCommand(
        CardType cardType,
        String title,
        String content,
        List<Long> fileIds
) {
}
