package com.fiveoutoffive.moa_backend.ideacard.application.command;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;
import java.util.List;
import java.util.UUID;

/**
 * sessionId: 카드를 넣을 회차. null 이면 저장하는 순간 진행 중인 회차에 넣는다.
 * requestId: 등록 재시도 키. null 이면 재시도 보호 없이 등록한다.
 */
public record IdeaCardCreateCommand(
        CardType cardType,
        String title,
        String content,
        List<Long> fileIds,
        Long sessionId,
        UUID requestId
) {

    /** 회차를 정하지 않는 생성(AI 회의 요약, 중재 채택). 진행 중인 회차에 넣는다. */
    public IdeaCardCreateCommand(CardType cardType, String title, String content, List<Long> fileIds) {
        this(cardType, title, content, fileIds, null, null);
    }
}
