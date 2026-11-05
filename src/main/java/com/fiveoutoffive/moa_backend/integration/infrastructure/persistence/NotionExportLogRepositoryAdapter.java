package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.integration.domain.model.NotionExportLog;
import com.fiveoutoffive.moa_backend.integration.domain.repository.NotionExportLogRepository;
import com.fiveoutoffive.moa_backend.integration.infrastructure.mapper.NotionExportLogMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotionExportLogRepositoryAdapter implements NotionExportLogRepository {

    private final SpringDataNotionExportLogRepository springDataRepository;
    private final NotionExportLogMapper notionExportLogMapper;

    @Override
    public NotionExportLog save(NotionExportLog notionExportLog) {
        NotionExportLogJpaEntity saved = springDataRepository.save(notionExportLogMapper.toJpaEntity(notionExportLog));
        return notionExportLogMapper.toDomain(saved);
    }

    @Override
    public Optional<NotionExportLog> findById(Long id) {
        return springDataRepository.findById(id)
                .map(notionExportLogMapper::toDomain);
    }
}
