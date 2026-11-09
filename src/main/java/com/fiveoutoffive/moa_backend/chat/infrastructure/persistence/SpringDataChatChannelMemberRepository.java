package com.fiveoutoffive.moa_backend.chat.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChannelMemberStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataChatChannelMemberRepository extends JpaRepository<ChatChannelMemberJpaEntity, Long> {

    Optional<ChatChannelMemberJpaEntity> findByChannelIdAndAccountId(Long channelId, Long accountId);

    List<ChatChannelMemberJpaEntity> findByChannelIdAndStatusIn(Long channelId, Collection<ChannelMemberStatus> statuses);

    List<ChatChannelMemberJpaEntity> findByAccountIdAndChannelIdIn(Long accountId, Collection<Long> channelIds);
}
