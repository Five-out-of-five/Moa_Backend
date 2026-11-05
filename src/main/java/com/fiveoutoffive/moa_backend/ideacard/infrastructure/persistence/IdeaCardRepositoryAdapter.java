package com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.IdeaCard;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.IdeaCardRepository;
import com.fiveoutoffive.moa_backend.ideacard.infrastructure.mapper.IdeaCardMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IdeaCardRepositoryAdapter implements IdeaCardRepository {

    private final SpringDataIdeaCardRepository springDataRepository;
    private final IdeaCardMapper ideaCardMapper;

    @Override
    public IdeaCard save(IdeaCard ideaCard) {
        IdeaCardJpaEntity saved = springDataRepository.save(ideaCardMapper.toJpaEntity(ideaCard));
        return ideaCardMapper.toDomain(saved);
    }

    @Override
    public Optional<IdeaCard> findById(Long id) {
        return springDataRepository.findById(id)
                .map(ideaCardMapper::toDomain);
    }
}
