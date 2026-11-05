package com.fiveoutoffive.moa_backend.chat.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataChatMessageRepository extends JpaRepository<ChatMessageJpaEntity, Long> {
}
