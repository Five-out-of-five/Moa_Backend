package com.fiveoutoffive.moa_backend.ai.application.command;

import com.fiveoutoffive.moa_backend.ai.domain.model.SummaryScopeType;
import java.util.List;

public record SummaryCreateCommand(
        SummaryScopeType scopeType,
        List<Long> sessionIds
) {
}
