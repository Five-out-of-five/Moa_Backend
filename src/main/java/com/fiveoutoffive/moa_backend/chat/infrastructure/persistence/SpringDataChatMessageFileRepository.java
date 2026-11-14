package com.fiveoutoffive.moa_backend.chat.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataChatMessageFileRepository extends JpaRepository<ChatMessageFileJpaEntity, Long> {

    List<ChatMessageFileJpaEntity> findByMessageIdInOrderBySortOrderAsc(Collection<Long> messageIds);

    @Query("select f.fileId from ChatMessageFileJpaEntity f where f.messageId in (select m.id from ChatMessageJpaEntity m where m.channelId = :channelId)")
    List<Long> findFileIdsByChannelId(@Param("channelId") Long channelId);
}
