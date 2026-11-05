package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ai.domain.model.ContentEmbedding;
import com.fiveoutoffive.moa_backend.ai.domain.repository.ContentEmbeddingRepository;
import com.fiveoutoffive.moa_backend.ai.infrastructure.mapper.ContentEmbeddingMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
}
