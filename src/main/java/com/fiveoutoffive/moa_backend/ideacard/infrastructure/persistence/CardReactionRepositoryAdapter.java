package com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardReaction;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.CardReactionRepository;
import com.fiveoutoffive.moa_backend.ideacard.infrastructure.mapper.CardReactionMapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CardReactionRepositoryAdapter implements CardReactionRepository {

    private final SpringDataCardReactionRepository springDataRepository;
    private final CardReactionMapper cardReactionMapper;

    @Override
    public CardReaction save(CardReaction cardReaction) {
        CardReactionJpaEntity saved = springDataRepository.save(cardReactionMapper.toJpaEntity(cardReaction));
        return cardReactionMapper.toDomain(saved);
    }

    @Override
    public Optional<CardReaction> findById(Long id) {
        return springDataRepository.findById(id)
                .map(cardReactionMapper::toDomain);
    }

    @Override
    public List<CardReaction> findByCardId(Long cardId) {
        return springDataRepository.findByCardId(cardId).stream().map(cardReactionMapper::toDomain).toList();
    }

    @Override
    public List<CardReaction> findByCardIdIn(Collection<Long> cardIds) {
        return springDataRepository.findByCardIdIn(cardIds).stream().map(cardReactionMapper::toDomain).toList();
    }

    @Override
    public Optional<CardReaction> findByCardIdAndAccountIdAndEmoji(Long cardId, Long accountId, String emoji) {
        return springDataRepository.findByCardIdAndAccountIdAndEmoji(cardId, accountId, emoji).map(cardReactionMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }
}
