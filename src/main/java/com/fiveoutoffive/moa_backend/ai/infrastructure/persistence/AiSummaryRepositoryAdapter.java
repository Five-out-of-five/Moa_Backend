package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiSummary;
import com.fiveoutoffive.moa_backend.ai.domain.repository.AiSummaryRepository;
import com.fiveoutoffive.moa_backend.ai.infrastructure.mapper.AiSummaryMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AiSummaryRepositoryAdapter implements AiSummaryRepository {

    private final SpringDataAiSummaryRepository springDataRepository;
    private final AiSummaryMapper aiSummaryMapper;

    @Override
    public AiSummary save(AiSummary aiSummary) {
        AiSummaryJpaEntity saved = springDataRepository.save(aiSummaryMapper.toJpaEntity(aiSummary));
        return aiSummaryMapper.toDomain(saved);
    }

    @Override
    public Optional<AiSummary> findById(Long id) {
        return springDataRepository.findById(id)
                .map(aiSummaryMapper::toDomain);
    }
}
