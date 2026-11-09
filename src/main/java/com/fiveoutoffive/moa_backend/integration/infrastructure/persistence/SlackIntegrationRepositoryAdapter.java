package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.integration.domain.model.SlackIntegration;
import com.fiveoutoffive.moa_backend.integration.domain.repository.SlackIntegrationRepository;
import com.fiveoutoffive.moa_backend.integration.infrastructure.mapper.SlackIntegrationMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SlackIntegrationRepositoryAdapter implements SlackIntegrationRepository {

    private final SpringDataSlackIntegrationRepository springDataRepository;
    private final SlackIntegrationMapper slackIntegrationMapper;

    @Override
    public SlackIntegration save(SlackIntegration slackIntegration) {
        SlackIntegrationJpaEntity saved = springDataRepository.save(slackIntegrationMapper.toJpaEntity(slackIntegration));
        return slackIntegrationMapper.toDomain(saved);
    }

    @Override
    public Optional<SlackIntegration> findById(Long id) {
        return springDataRepository.findById(id)
                .map(slackIntegrationMapper::toDomain);
    }

    @Override
    public Optional<SlackIntegration> findByRoomId(Long roomId) {
        return springDataRepository.findByRoomId(roomId).map(slackIntegrationMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }
}
