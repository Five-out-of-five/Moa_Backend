package com.fiveoutoffive.moa_backend.report.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataReportRepository extends JpaRepository<ReportJpaEntity, Long> {
}
