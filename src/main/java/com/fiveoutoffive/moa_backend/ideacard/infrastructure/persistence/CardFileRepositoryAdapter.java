package com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardFile;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.CardFileRepository;
import com.fiveoutoffive.moa_backend.ideacard.infrastructure.mapper.CardFileMapper;
import java.util.Collection;
import java.util.List;
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

    @Override
    public List<CardFile> findByCardIdOrderBySortOrderAsc(Long cardId) {
        return springDataRepository.findByCardIdOrderBySortOrderAsc(cardId).stream().map(cardFileMapper::toDomain).toList();
    }

    @Override
    public List<CardFile> findByCardIdIn(Collection<Long> cardIds) {
        return springDataRepository.findByCardIdIn(cardIds).stream().map(cardFileMapper::toDomain).toList();
    }

    @Override
    public Optional<CardFile> findByCardIdAndFileId(Long cardId, Long fileId) {
        return springDataRepository.findByCardIdAndFileId(cardId, fileId).map(cardFileMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }
}
