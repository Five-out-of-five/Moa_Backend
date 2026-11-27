package com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataIdeaCardRepository extends JpaRepository<IdeaCardJpaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from IdeaCardJpaEntity c where c.id = :id and c.deletedAt is null")
    Optional<IdeaCardJpaEntity> findActiveByIdForUpdate(@Param("id") Long id);

    // 트랜잭션 범위 advisory lock. pg_advisory_xact_lock 은 void 를 돌려줘서 바로 매핑할 수 없으므로 감싸서 센다.
    @Query(value = "select count(*) from (select pg_advisory_xact_lock(hashtextextended(:key, 0))) l", nativeQuery = true)
    long lockCreateRequest(@Param("key") String key);

    Optional<IdeaCardJpaEntity> findByWriterAccountIdAndRequestId(Long writerAccountId, UUID requestId);

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
