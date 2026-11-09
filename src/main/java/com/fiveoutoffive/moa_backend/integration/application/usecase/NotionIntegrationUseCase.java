package com.fiveoutoffive.moa_backend.integration.application.usecase;

import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.integration.application.command.NotionExportCommand;
import com.fiveoutoffive.moa_backend.integration.application.command.NotionIntegrationUpdateCommand;
import com.fiveoutoffive.moa_backend.integration.application.result.NotionAuthorizeResponse;
import com.fiveoutoffive.moa_backend.integration.application.result.NotionDatabaseResponse;
import com.fiveoutoffive.moa_backend.integration.application.result.NotionExportResponse;
import com.fiveoutoffive.moa_backend.integration.application.result.NotionIntegrationResponse;
import com.fiveoutoffive.moa_backend.session.domain.event.SessionClosedEvent;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface NotionIntegrationUseCase {

    NotionIntegrationResponse get(Long accountId, Long roomId);

    NotionAuthorizeResponse authorize(Long accountId, Long roomId);

    String callback(String code, String state);

    List<NotionDatabaseResponse> getDatabases(Long accountId, Long roomId);

    NotionIntegrationResponse update(Long accountId, Long roomId, NotionIntegrationUpdateCommand command);

    NotionExportResponse export(Long accountId, Long roomId, NotionExportCommand command);

    PageResponse<NotionExportResponse> getExports(Long accountId, Long roomId, Pageable pageable);

    void delete(Long accountId, Long roomId);

    void onSessionClosed(SessionClosedEvent event);
}
