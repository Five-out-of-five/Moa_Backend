package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiCallLog;
import com.fiveoutoffive.moa_backend.ai.domain.repository.AiCallLogRepository;
import com.fiveoutoffive.moa_backend.ai.infrastructure.mapper.AiCallLogMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AiCallLogRepositoryAdapter implements AiCallLogRepository {

    private final SpringDataAiCallLogRepository springDataRepository;
    private final AiCallLogMapper aiCallLogMapper;

    @Override
    public AiCallLog save(AiCallLog aiCallLog) {
        AiCallLogJpaEntity saved = springDataRepository.save(aiCallLogMapper.toJpaEntity(aiCallLog));
        return aiCallLogMapper.toDomain(saved);
    }

    @Override
    public Optional<AiCallLog> findById(Long id) {
        return springDataRepository.findById(id)
                .map(aiCallLogMapper::toDomain);
    }

    @Override
    public List<AiCallLog> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from, LocalDateTime to) {
        return springDataRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(from, to).stream().map(aiCallLogMapper::toDomain).toList();
    }
}
