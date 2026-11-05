package com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardFile;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.CardFileRepository;
import com.fiveoutoffive.moa_backend.ideacard.infrastructure.mapper.CardFileMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CardFileRepositoryAdapter implements CardFileRepository {

    private final SpringDataCardFileRepository springDataRepository;
    private final CardFileMapper cardFileMapper;

    @Override
    public CardFile save(CardFile cardFile) {
        CardFileJpaEntity saved = springDataRepository.save(cardFileMapper.toJpaEntity(cardFile));
        return cardFileMapper.toDomain(saved);
    }

    @Override
    public Optional<CardFile> findById(Long id) {
        return springDataRepository.findById(id)
                .map(cardFileMapper::toDomain);
    }
}
