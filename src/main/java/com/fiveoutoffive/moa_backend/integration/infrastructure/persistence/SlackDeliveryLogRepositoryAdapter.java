package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.integration.domain.model.SlackDeliveryLog;
import com.fiveoutoffive.moa_backend.integration.domain.repository.SlackDeliveryLogRepository;
import com.fiveoutoffive.moa_backend.integration.infrastructure.mapper.SlackDeliveryLogMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
}
