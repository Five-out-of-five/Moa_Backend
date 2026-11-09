package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.integration.domain.model.SlackDeliveryLog;
import com.fiveoutoffive.moa_backend.integration.domain.model.SlackDeliveryStatus;
import com.fiveoutoffive.moa_backend.integration.domain.repository.SlackDeliveryLogRepository;
import com.fiveoutoffive.moa_backend.integration.infrastructure.mapper.SlackDeliveryLogMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SlackDeliveryLogRepositoryAdapter implements SlackDeliveryLogRepository {

    private final SpringDataSlackDeliveryLogRepository springDataRepository;
    private final SlackDeliveryLogMapper slackDeliveryLogMapper;

    @Override
    public SlackDeliveryLog save(SlackDeliveryLog slackDeliveryLog) {
        SlackDeliveryLogJpaEntity saved = springDataRepository.save(slackDeliveryLogMapper.toJpaEntity(slackDeliveryLog));
        return slackDeliveryLogMapper.toDomain(saved);
    }

    @Override
    public Optional<SlackDeliveryLog> findById(Long id) {
        return springDataRepository.findById(id)
                .map(slackDeliveryLogMapper::toDomain);
    }

    @Override
    public Page<SlackDeliveryLog> findByIntegrationId(Long integrationId, Pageable pageable) {
        return springDataRepository.findByIntegrationId(integrationId, pageable).map(slackDeliveryLogMapper::toDomain);
    }

    @Override
    public Page<SlackDeliveryLog> findByIntegrationIdAndStatus(Long integrationId, SlackDeliveryStatus status, Pageable pageable) {
        return springDataRepository.findByIntegrationIdAndStatus(integrationId, status, pageable).map(slackDeliveryLogMapper::toDomain);
    }
}
