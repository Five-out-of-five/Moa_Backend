package com.fiveoutoffive.moa_backend.ai.application.result;

import com.fiveoutoffive.moa_backend.ai.domain.model.SummaryScopeType;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import java.time.LocalDateTime;
import java.util.List;

public record SummaryResponse(Long summaryId, SummaryScopeType scopeType, List<SessionRef> sessions, String content, MemberRef requestedBy, Long savedCardId, LocalDateTime createdAt, LocalDateTime updatedAt) {

    public record SessionRef(Long sessionId, Integer sessionNo, String title) {
    }
}
