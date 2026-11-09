package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.integration.domain.model.NotionIntegration;
import com.fiveoutoffive.moa_backend.integration.domain.repository.NotionIntegrationRepository;
import com.fiveoutoffive.moa_backend.integration.infrastructure.mapper.NotionIntegrationMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotionIntegrationRepositoryAdapter implements NotionIntegrationRepository {

    private final SpringDataNotionIntegrationRepository springDataRepository;
    private final NotionIntegrationMapper notionIntegrationMapper;

    @Override
    public NotionIntegration save(NotionIntegration notionIntegration) {
        NotionIntegrationJpaEntity saved = springDataRepository.save(notionIntegrationMapper.toJpaEntity(notionIntegration));
        return notionIntegrationMapper.toDomain(saved);
    }

    @Override
    public Optional<NotionIntegration> findById(Long id) {
        return springDataRepository.findById(id)
                .map(notionIntegrationMapper::toDomain);
    }

    @Override
    public Optional<NotionIntegration> findByRoomId(Long roomId) {
        return springDataRepository.findByRoomId(roomId).map(notionIntegrationMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }
}
