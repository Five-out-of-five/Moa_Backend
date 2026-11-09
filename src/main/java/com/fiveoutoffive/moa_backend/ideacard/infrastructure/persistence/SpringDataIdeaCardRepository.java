package com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataIdeaCardRepository extends JpaRepository<IdeaCardJpaEntity, Long> {

    List<IdeaCardJpaEntity> findBySessionIdAndDeletedAtIsNullOrderBySortOrderAsc(Long sessionId);

    List<IdeaCardJpaEntity> findBySessionIdAndCardTypeAndDeletedAtIsNullOrderBySortOrderAsc(Long sessionId, CardType cardType);

    long countBySessionIdAndDeletedAtIsNull(Long sessionId);

    long countByRoomIdAndSessionIdIn(Long roomId, Collection<Long> sessionIds);

    List<IdeaCardJpaEntity> findByRoomIdAndDeletedAtIsNull(Long roomId);

    List<IdeaCardJpaEntity> findByRoomIdAndSessionIdInAndDeletedAtIsNull(Long roomId, Collection<Long> sessionIds);

    List<IdeaCardJpaEntity> findByIdIn(Collection<Long> ids);

    @Query("select count(distinct c.writerAccountId) from IdeaCardJpaEntity c where c.sessionId = :sessionId and c.deletedAt is null")
    long countWritersBySessionId(@Param("sessionId") Long sessionId);

    @Query("select coalesce(max(c.sortOrder), -1) from IdeaCardJpaEntity c where c.sessionId = :sessionId and c.cardType = :cardType and c.deletedAt is null")
    int findMaxSortOrder(@Param("sessionId") Long sessionId, @Param("cardType") CardType cardType);
}
