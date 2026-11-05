package com.fiveoutoffive.moa_backend.report.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.report.domain.model.Report;
import com.fiveoutoffive.moa_backend.report.domain.repository.ReportRepository;
import com.fiveoutoffive.moa_backend.report.infrastructure.mapper.ReportMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
}
