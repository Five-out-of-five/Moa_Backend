package com.fiveoutoffive.moa_backend.ideacard.presentation.api.request;

import com.fiveoutoffive.moa_backend.ideacard.application.command.IdeaCardCreateCommand;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record IdeaCardCreateRequest(
        @NotNull CardType cardType,
        @NotBlank @Size(max=100) String title,
        @Size(max=1000) String content,
        @Size(max=5) List<Long> fileIds
) {

    public IdeaCardCreateCommand toCommand() {
        return new IdeaCardCreateCommand(cardType, title, content, fileIds);
    }
}
