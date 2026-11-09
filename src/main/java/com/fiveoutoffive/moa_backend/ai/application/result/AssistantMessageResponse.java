package com.fiveoutoffive.moa_backend.ai.application.result;

import com.fiveoutoffive.moa_backend.ai.domain.model.AssistantMessageStatus;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import java.time.LocalDateTime;

public record AssistantMessageResponse(Long messageId, MemberRef asker, String question, String answer, AssistantMessageStatus status, String references, LocalDateTime createdAt) {
}
