package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ai.domain.model.ContentEmbedding;
import com.fiveoutoffive.moa_backend.ai.domain.repository.ContentEmbeddingRepository;
import com.fiveoutoffive.moa_backend.ai.infrastructure.mapper.ContentEmbeddingMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ContentEmbeddingRepositoryAdapter implements ContentEmbeddingRepository {

    private final SpringDataContentEmbeddingRepository springDataRepository;
    private final ContentEmbeddingMapper contentEmbeddingMapper;

    @Override
    public ContentEmbedding save(ContentEmbedding contentEmbedding) {
        ContentEmbeddingJpaEntity saved = springDataRepository.save(contentEmbeddingMapper.toJpaEntity(contentEmbedding));
        return contentEmbeddingMapper.toDomain(saved);
    }

    @Override
    public Optional<ContentEmbedding> findById(Long id) {
        return springDataRepository.findById(id)
                .map(contentEmbeddingMapper::toDomain);
    }

    @Override
    public int upsertCard(@Param("roomId") Long roomId, @Param("cardId") Long cardId, @Param("vector") String vector, @Param("model") String model, @Param("hash") String hash) {
        return springDataRepository.upsertCard(roomId, cardId, vector, model, hash);
    }

    @Override
    public Optional<String> findHashByCardId(@Param("cardId") Long cardId) {
        return springDataRepository.findHashByCardId(cardId);
    }

    @Override
    public List<Long> findNearestCardIds(@Param("roomId") Long roomId, @Param("cardId") Long cardId, @Param("limit") int limit) {
        return springDataRepository.findNearestCardIds(roomId, cardId, limit);
    }

    @Override
    public int deleteByCardId(@Param("cardId") Long cardId) {
        return springDataRepository.deleteByCardId(cardId);
    }
}
