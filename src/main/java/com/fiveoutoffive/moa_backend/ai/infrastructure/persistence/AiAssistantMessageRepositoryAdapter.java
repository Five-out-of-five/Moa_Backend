package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiAssistantMessage;
import com.fiveoutoffive.moa_backend.ai.domain.repository.AiAssistantMessageRepository;
import com.fiveoutoffive.moa_backend.ai.infrastructure.mapper.AiAssistantMessageMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AiAssistantMessageRepositoryAdapter implements AiAssistantMessageRepository {

    private final SpringDataAiAssistantMessageRepository springDataRepository;
    private final AiAssistantMessageMapper aiAssistantMessageMapper;

    @Override
    public AiAssistantMessage save(AiAssistantMessage aiAssistantMessage) {
        AiAssistantMessageJpaEntity saved = springDataRepository.save(aiAssistantMessageMapper.toJpaEntity(aiAssistantMessage));
        return aiAssistantMessageMapper.toDomain(saved);
    }

    @Override
    public Optional<AiAssistantMessage> findById(Long id) {
        return springDataRepository.findById(id)
                .map(aiAssistantMessageMapper::toDomain);
    }

    @Override
    public List<AiAssistantMessage> findMine(@Param("roomId") Long roomId, @Param("accountId") Long accountId, @Param("before") Long before, @Param("limit") int limit) {
        return springDataRepository.findMine(roomId, accountId, before, limit).stream().map(aiAssistantMessageMapper::toDomain).toList();
    }
}
