package com.fiveoutoffive.moa_backend.chat.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataChatChannelRepository extends JpaRepository<ChatChannelJpaEntity, Long> {

    List<ChatChannelJpaEntity> findByRoomIdAndDeletedAtIsNull(Long roomId);

    Optional<ChatChannelJpaEntity> findFirstByRoomIdAndIsDefaultTrue(Long roomId);

    Optional<ChatChannelJpaEntity> findByRoomIdAndDmKey(Long roomId, String dmKey);

    boolean existsByRoomIdAndNameAndDeletedAtIsNull(Long roomId, String name);
}
