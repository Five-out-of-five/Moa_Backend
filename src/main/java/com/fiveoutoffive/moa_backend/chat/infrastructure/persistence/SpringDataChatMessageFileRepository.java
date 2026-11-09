package com.fiveoutoffive.moa_backend.chat.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataChatMessageFileRepository extends JpaRepository<ChatMessageFileJpaEntity, Long> {

    List<ChatMessageFileJpaEntity> findByMessageIdInOrderBySortOrderAsc(Collection<Long> messageIds);
}
