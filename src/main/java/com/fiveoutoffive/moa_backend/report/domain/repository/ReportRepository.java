package com.fiveoutoffive.moa_backend.report.domain.repository;

import com.fiveoutoffive.moa_backend.report.domain.model.Report;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportReason;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportStatus;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportTargetType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface ReportRepository {

    Report save(Report report);

    Optional<Report> findById(Long id);

    boolean existsByReporterAccountIdAndTargetTypeAndTargetId(Long reporterAccountId, ReportTargetType targetType, Long targetId);

    long countByRoomId(Long roomId);

    long countFalseReports(@Param("reporter") Long reporter, @Param("since") LocalDateTime since);

    Page<Report> search(@Param("status") ReportStatus status, @Param("targetType") ReportTargetType targetType, @Param("reason") ReportReason reason, Pageable pageable);
}
