package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiSummarySession;
import com.fiveoutoffive.moa_backend.ai.domain.repository.AiSummarySessionRepository;
import com.fiveoutoffive.moa_backend.ai.infrastructure.mapper.AiSummarySessionMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AiSummarySessionRepositoryAdapter implements AiSummarySessionRepository {

    private final SpringDataAiSummarySessionRepository springDataRepository;
    private final AiSummarySessionMapper aiSummarySessionMapper;

    @Override
    public AiSummarySession save(AiSummarySession aiSummarySession) {
        AiSummarySessionJpaEntity saved = springDataRepository.save(aiSummarySessionMapper.toJpaEntity(aiSummarySession));
        return aiSummarySessionMapper.toDomain(saved);
    }

    @Override
    public List<AiSummarySession> findBySummaryId(Long summaryId) {
        return springDataRepository.findBySummaryId(summaryId).stream().map(aiSummarySessionMapper::toDomain).toList();
    }
}
