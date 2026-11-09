package com.fiveoutoffive.moa_backend.ai.application.usecase;

import com.fiveoutoffive.moa_backend.ai.application.command.AssistantAskCommand;
import com.fiveoutoffive.moa_backend.ai.application.command.SummaryCreateCommand;
import com.fiveoutoffive.moa_backend.ai.application.command.SummaryToCardCommand;
import com.fiveoutoffive.moa_backend.ai.application.command.SummaryUpdateCommand;
import com.fiveoutoffive.moa_backend.ai.application.command.ToneCheckCommand;
import com.fiveoutoffive.moa_backend.ai.application.result.AiQuotaResponse;
import com.fiveoutoffive.moa_backend.ai.application.result.AssistantMessageResponse;
import com.fiveoutoffive.moa_backend.ai.application.result.SummaryResponse;
import com.fiveoutoffive.moa_backend.ai.application.result.ToneCheckResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.CursorResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.IdeaCardDetailResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiUseCase {

    SseEmitter ask(Long accountId, Long roomId, AssistantAskCommand command);

    CursorResponse<AssistantMessageResponse> getHistory(Long accountId, Long roomId, Long before, int size);

    AiQuotaResponse getQuota(Long accountId, Long roomId);

    SummaryResponse createSummary(Long accountId, Long roomId, SummaryCreateCommand command);

    PageResponse<SummaryResponse> getSummaries(Long accountId, Long roomId, Pageable pageable);

    SummaryResponse updateSummary(Long accountId, Long roomId, Long summaryId, SummaryUpdateCommand command);

    IdeaCardDetailResponse saveSummaryAsCard(Long accountId, Long roomId, Long summaryId, SummaryToCardCommand command);

    ToneCheckResponse checkTone(Long accountId, Long roomId, ToneCheckCommand command);
}
