package com.fiveoutoffive.moa_backend.mediation.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataCardConflictRepository extends JpaRepository<CardConflictJpaEntity, Long> {

    List<CardConflictJpaEntity> findByRoomId(Long roomId);

    Optional<CardConflictJpaEntity> findByCardAIdAndCardBId(Long cardAId, Long cardBId);

    @Modifying
    @Query("delete from CardConflictJpaEntity c where c.cardAId = :cardId or c.cardBId = :cardId")
    int deleteByCardId(@Param("cardId") Long cardId);
}
