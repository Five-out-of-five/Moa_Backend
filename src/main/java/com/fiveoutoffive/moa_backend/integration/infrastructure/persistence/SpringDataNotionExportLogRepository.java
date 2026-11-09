package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotionExportLogRepository extends JpaRepository<NotionExportLogJpaEntity, Long> {

    Page<NotionExportLogJpaEntity> findByIntegrationId(Long integrationId, Pageable pageable);

    Optional<NotionExportLogJpaEntity> findByIntegrationIdAndSessionId(Long integrationId, Long sessionId);
}
