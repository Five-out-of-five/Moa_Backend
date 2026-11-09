package com.fiveoutoffive.moa_backend.report.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.report.domain.model.ReportReason;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportStatus;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportTargetType;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataReportRepository extends JpaRepository<ReportJpaEntity, Long> {

    boolean existsByReporterAccountIdAndTargetTypeAndTargetId(Long reporterAccountId, ReportTargetType targetType, Long targetId);

    long countByRoomId(Long roomId);

    @Query("select count(r) from ReportJpaEntity r where r.reporterAccountId = :reporter and r.isFalseReport = true and r.handledAt >= :since")
    long countFalseReports(@Param("reporter") Long reporter, @Param("since") LocalDateTime since);

    @Query("select r from ReportJpaEntity r where (:status is null or r.status = :status) and (:targetType is null or r.targetType = :targetType) and (:reason is null or r.reason = :reason)")
    Page<ReportJpaEntity> search(@Param("status") ReportStatus status, @Param("targetType") ReportTargetType targetType, @Param("reason") ReportReason reason, Pageable pageable);
}
