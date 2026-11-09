package com.fiveoutoffive.moa_backend.integration.application.usecase;

import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.integration.application.command.SlackIntegrationCommand;
import com.fiveoutoffive.moa_backend.integration.application.command.SlackTestCommand;
import com.fiveoutoffive.moa_backend.integration.application.result.SlackDeliveryResponse;
import com.fiveoutoffive.moa_backend.integration.application.result.SlackIntegrationResponse;
import com.fiveoutoffive.moa_backend.integration.domain.model.SlackDeliveryStatus;
import org.springframework.data.domain.Pageable;

public interface SlackIntegrationUseCase {

    SlackIntegrationResponse get(Long accountId, Long roomId);

    void test(Long accountId, Long roomId, SlackTestCommand command);

    SlackIntegrationResponse upsert(Long accountId, Long roomId, SlackIntegrationCommand command);

    void delete(Long accountId, Long roomId);

    PageResponse<SlackDeliveryResponse> getDeliveries(Long accountId, Long roomId, SlackDeliveryStatus status, Pageable pageable);
}
