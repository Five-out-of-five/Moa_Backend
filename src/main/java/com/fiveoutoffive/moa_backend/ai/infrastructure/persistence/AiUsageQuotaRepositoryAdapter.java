package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiUsageQuota;
import com.fiveoutoffive.moa_backend.ai.domain.repository.AiUsageQuotaRepository;
import com.fiveoutoffive.moa_backend.ai.infrastructure.mapper.AiUsageQuotaMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AiUsageQuotaRepositoryAdapter implements AiUsageQuotaRepository {

    private final SpringDataAiUsageQuotaRepository springDataRepository;
    private final AiUsageQuotaMapper aiUsageQuotaMapper;

    @Override
    public AiUsageQuota save(AiUsageQuota aiUsageQuota) {
        AiUsageQuotaJpaEntity saved = springDataRepository.save(aiUsageQuotaMapper.toJpaEntity(aiUsageQuota));
        return aiUsageQuotaMapper.toDomain(saved);
    }

    @Override
    public Optional<AiUsageQuota> findById(Long id) {
        return springDataRepository.findById(id)
                .map(aiUsageQuotaMapper::toDomain);
    }
}
