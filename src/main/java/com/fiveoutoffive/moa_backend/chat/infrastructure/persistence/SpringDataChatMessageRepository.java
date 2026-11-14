package com.fiveoutoffive.moa_backend.chat.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataChatMessageRepository extends JpaRepository<ChatMessageJpaEntity, Long> {

    @Query("select m from ChatMessageJpaEntity m where m.channelId = :channelId and (:before is null or m.id < :before) order by m.id desc limit :limit")
    List<ChatMessageJpaEntity> findBefore(@Param("channelId") Long channelId, @Param("before") Long before, @Param("limit") int limit);

    @Query("select m from ChatMessageJpaEntity m where m.channelId = :channelId and m.id > :after order by m.id asc limit :limit")
    List<ChatMessageJpaEntity> findAfter(@Param("channelId") Long channelId, @Param("after") Long after, @Param("limit") int limit);

    @Query("select count(m) from ChatMessageJpaEntity m where m.channelId = :channelId and m.id > :lastRead and m.deletedAt is null and (m.senderAccountId is null or m.senderAccountId <> :me)")
    long countUnread(@Param("channelId") Long channelId, @Param("lastRead") long lastRead, @Param("me") Long me);

    @Modifying
    @Query("update ChatMessageJpaEntity m set m.deletedAt = :deletedAt where m.channelId = :channelId and m.deletedAt is null")
    int softDeleteByChannelId(@Param("channelId") Long channelId, @Param("deletedAt") LocalDateTime deletedAt);

    long countByDeletedAtIsNull();

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from, LocalDateTime to);
}
