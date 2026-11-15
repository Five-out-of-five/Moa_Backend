package com.fiveoutoffive.moa_backend.ai.application.result;

import com.fiveoutoffive.moa_backend.ai.domain.model.AssistantMessageStatus;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import java.time.LocalDateTime;
import tools.jackson.databind.JsonNode;

/** references 는 질문 스트림의 references 이벤트와 같은 객체 {cards, tasks, votes} 다. 없으면 null. */
public record AssistantMessageResponse(Long messageId, MemberRef asker, String question, String answer, AssistantMessageStatus status, JsonNode references, LocalDateTime createdAt) {
}
