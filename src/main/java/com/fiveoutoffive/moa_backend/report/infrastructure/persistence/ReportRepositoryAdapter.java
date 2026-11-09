package com.fiveoutoffive.moa_backend.report.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.report.domain.model.Report;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportReason;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportStatus;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportTargetType;
import com.fiveoutoffive.moa_backend.report.domain.repository.ReportRepository;
import com.fiveoutoffive.moa_backend.report.infrastructure.mapper.ReportMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryAdapter implements ReportRepository {

    private final SpringDataReportRepository springDataRepository;
    private final ReportMapper reportMapper;

    @Override
    public Report save(Report report) {
        ReportJpaEntity saved = springDataRepository.save(reportMapper.toJpaEntity(report));
        return reportMapper.toDomain(saved);
    }

    @Override
    public Optional<Report> findById(Long id) {
        return springDataRepository.findById(id)
                .map(reportMapper::toDomain);
    }

    @Override
    public boolean existsByReporterAccountIdAndTargetTypeAndTargetId(Long reporterAccountId, ReportTargetType targetType, Long targetId) {
        return springDataRepository.existsByReporterAccountIdAndTargetTypeAndTargetId(reporterAccountId, targetType, targetId);
    }

    @Override
    public long countByRoomId(Long roomId) {
        return springDataRepository.countByRoomId(roomId);
    }

    @Override
    public long countFalseReports(@Param("reporter") Long reporter, @Param("since") LocalDateTime since) {
        return springDataRepository.countFalseReports(reporter, since);
    }

    @Override
    public Page<Report> search(@Param("status") ReportStatus status, @Param("targetType") ReportTargetType targetType, @Param("reason") ReportReason reason, Pageable pageable) {
        return springDataRepository.search(status, targetType, reason, pageable).map(reportMapper::toDomain);
    }
}
