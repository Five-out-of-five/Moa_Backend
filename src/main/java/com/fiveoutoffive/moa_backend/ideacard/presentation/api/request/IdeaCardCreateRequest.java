package com.fiveoutoffive.moa_backend.ideacard.presentation.api.request;

import com.fiveoutoffive.moa_backend.ideacard.application.command.IdeaCardCreateCommand;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record IdeaCardCreateRequest(
        @NotNull CardType cardType,
        @NotBlank @Size(max=100) String title,
        @Size(max=1000) String content,
        @Size(max=5) List<Long> fileIds,
        @Schema(description = "작성하던 회차 ID. 보내면 저장할 때 이 회차가 아직 진행 중인지 확인하고, 마감됐으면 409 CARD_SESSION_CLOSED, 다른 방이거나 없는 회차면 404 SESSION_NOT_FOUND. 생략하면 저장하는 순간 진행 중인 회차에 넣는다.")
        Long sessionId
) {

    public IdeaCardCreateCommand toCommand() {
        return new IdeaCardCreateCommand(cardType, title, content, fileIds, sessionId);
    }
}
