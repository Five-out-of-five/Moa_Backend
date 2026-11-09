package com.fiveoutoffive.moa_backend.ai.presentation.api.request;

import com.fiveoutoffive.moa_backend.ai.application.command.SummaryCreateCommand;
import com.fiveoutoffive.moa_backend.ai.domain.model.SummaryScopeType;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SummaryCreateRequest(
        @NotNull SummaryScopeType scopeType,
        List<Long> sessionIds
) {

    public SummaryCreateCommand toCommand() {
        return new SummaryCreateCommand(scopeType, sessionIds);
    }
}
