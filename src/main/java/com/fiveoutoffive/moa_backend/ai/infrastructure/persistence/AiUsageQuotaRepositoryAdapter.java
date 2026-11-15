package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiUsageQuota;
import com.fiveoutoffive.moa_backend.ai.domain.repository.AiUsageQuotaRepository;
import com.fiveoutoffive.moa_backend.ai.infrastructure.mapper.AiUsageQuotaMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    // 선점과 반납은 바깥 트랜잭션과 따로 바로 커밋한다. 다른 요청이 곧바로 늘어난 사용량을 봐야 한다.
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryReserve(Long roomId, AiFeature feature, LocalDate date, int limit) {
        return springDataRepository.tryReserve(roomId, feature.name(), date, limit) > 0;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void release(Long roomId, AiFeature feature, LocalDate date) {
        springDataRepository.release(roomId, feature.name(), date);
    }
}
