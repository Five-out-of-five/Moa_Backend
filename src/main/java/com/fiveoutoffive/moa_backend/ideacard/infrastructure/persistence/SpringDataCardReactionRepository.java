package com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataCardReactionRepository extends JpaRepository<CardReactionJpaEntity, Long> {

    List<CardReactionJpaEntity> findByCardId(Long cardId);

    List<CardReactionJpaEntity> findByCardIdIn(Collection<Long> cardIds);

    Optional<CardReactionJpaEntity> findByCardIdAndAccountIdAndEmoji(Long cardId, Long accountId, String emoji);

    void deleteById(Long id);

    /** 이미 있으면 아무것도 하지 않는다. 같은 이모지를 동시에 두 번 눌러도 1행만 남고 예외가 나지 않는다. */
    @Modifying
    @Query(value = "insert into card_reaction (card_id, account_id, emoji) values (:cardId, :accountId, :emoji) on conflict (card_id, account_id, emoji) do nothing", nativeQuery = true)
    int insertIfAbsent(@Param("cardId") Long cardId, @Param("accountId") Long accountId, @Param("emoji") String emoji);
}
