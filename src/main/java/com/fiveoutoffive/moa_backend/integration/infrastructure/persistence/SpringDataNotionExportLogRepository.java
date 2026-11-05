package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotionExportLogRepository extends JpaRepository<NotionExportLogJpaEntity, Long> {
}
