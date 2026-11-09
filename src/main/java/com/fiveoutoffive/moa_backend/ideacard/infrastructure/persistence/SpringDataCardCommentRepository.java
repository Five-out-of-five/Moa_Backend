package com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataCardCommentRepository extends JpaRepository<CardCommentJpaEntity, Long> {

    List<CardCommentJpaEntity> findByCardIdOrderByCreatedAtAsc(Long cardId);

    List<CardCommentJpaEntity> findByCardIdAndDeletedAtIsNull(Long cardId);

    @Query("select c.cardId, count(c) from CardCommentJpaEntity c where c.cardId in :ids and c.deletedAt is null group by c.cardId")
    List<Object[]> countByCardIds(@Param("ids") Collection<Long> ids);
}
