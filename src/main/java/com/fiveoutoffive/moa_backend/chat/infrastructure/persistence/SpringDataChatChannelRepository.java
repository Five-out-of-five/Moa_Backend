package com.fiveoutoffive.moa_backend.chat.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataChatChannelRepository extends JpaRepository<ChatChannelJpaEntity, Long> {
}
