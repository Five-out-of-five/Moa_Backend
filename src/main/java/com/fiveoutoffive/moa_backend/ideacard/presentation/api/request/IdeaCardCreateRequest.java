package com.fiveoutoffive.moa_backend.ideacard.presentation.api.request;

import com.fiveoutoffive.moa_backend.ideacard.application.command.IdeaCardCreateCommand;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record IdeaCardCreateRequest(
        @NotNull CardType cardType,
        @NotBlank @Size(max=100) String title,
        @Size(max=1000) String content,
        @Size(max=5) List<Long> fileIds,
        @Schema(description = "작성하던 회차 ID. 보내면 저장할 때 이 회차가 아직 진행 중인지 확인하고, 마감됐으면 409 CARD_SESSION_CLOSED, 다른 방이거나 없는 회차면 404 SESSION_NOT_FOUND. 생략하면 저장하는 순간 진행 중인 회차에 넣는다.")
        Long sessionId,
        @Schema(description = "등록 재시도 키(UUID). 처음 저장할 때 한 번 만들고, 응답을 못 받았으면(타임아웃·5xx) 같은 본문과 같은 키로 다시 보낸다. "
                + "이미 저장됐으면 새로 만들지 않고 처음 카드를 200 으로 돌려준다. 같은 키에 다른 본문이면 409 CARD_REQUEST_ID_CONFLICT, "
                + "처음 카드가 삭제됐으면 404 CARD_NOT_FOUND. 재시도는 24시간 안에서 한다. 생략하면 재시도 보호 없이 등록한다.")
        UUID requestId
) {

    public IdeaCardCreateCommand toCommand() {
        return new IdeaCardCreateCommand(cardType, title, content, fileIds, sessionId, requestId);
    }
}
