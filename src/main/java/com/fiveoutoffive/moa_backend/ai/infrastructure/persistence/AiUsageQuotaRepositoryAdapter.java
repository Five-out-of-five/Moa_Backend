package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiUsageQuota;
import com.fiveoutoffive.moa_backend.ai.domain.repository.AiUsageQuotaRepository;
import com.fiveoutoffive.moa_backend.ai.infrastructure.mapper.AiUsageQuotaMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
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

    @Override
    public Optional<AiUsageQuota> findByRoomIdAndFeatureAndQuotaDate(Long roomId, AiFeature feature, LocalDate quotaDate) {
        return springDataRepository.findByRoomIdAndFeatureAndQuotaDate(roomId, feature, quotaDate).map(aiUsageQuotaMapper::toDomain);
    }

    @Override
    public List<AiUsageQuota> findByRoomIdAndQuotaDate(Long roomId, LocalDate quotaDate) {
        return springDataRepository.findByRoomIdAndQuotaDate(roomId, quotaDate).stream().map(aiUsageQuotaMapper::toDomain).toList();
    }

    @Override
    public int increment(@Param("roomId") Long roomId, @Param("feature") String feature, @Param("date") LocalDate date) {
        return springDataRepository.increment(roomId, feature, date);
    }
}
