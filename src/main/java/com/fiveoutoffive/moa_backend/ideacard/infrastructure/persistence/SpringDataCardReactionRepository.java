package com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCardReactionRepository extends JpaRepository<CardReactionJpaEntity, Long> {

    List<CardReactionJpaEntity> findByCardId(Long cardId);

    List<CardReactionJpaEntity> findByCardIdIn(Collection<Long> cardIds);

    Optional<CardReactionJpaEntity> findByCardIdAndAccountIdAndEmoji(Long cardId, Long accountId, String emoji);

    void deleteById(Long id);
}
