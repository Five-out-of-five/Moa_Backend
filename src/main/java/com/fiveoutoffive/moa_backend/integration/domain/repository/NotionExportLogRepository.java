package com.fiveoutoffive.moa_backend.integration.domain.repository;

import com.fiveoutoffive.moa_backend.integration.domain.model.NotionExportLog;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface NotionExportLogRepository {

    NotionExportLog save(NotionExportLog notionExportLog);

    Optional<NotionExportLog> findById(Long id);

    Page<NotionExportLog> findByIntegrationId(Long integrationId, Pageable pageable);

    Optional<NotionExportLog> findByIntegrationIdAndSessionId(Long integrationId, Long sessionId);
}
