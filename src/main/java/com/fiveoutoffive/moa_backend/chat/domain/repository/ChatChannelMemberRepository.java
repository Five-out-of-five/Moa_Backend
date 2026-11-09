package com.fiveoutoffive.moa_backend.chat.domain.repository;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChannelMemberStatus;
import com.fiveoutoffive.moa_backend.chat.domain.model.ChatChannelMember;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface ChatChannelMemberRepository {

    ChatChannelMember save(ChatChannelMember chatChannelMember);

    Optional<ChatChannelMember> findById(Long id);

    Optional<ChatChannelMember> findByChannelIdAndAccountId(Long channelId, Long accountId);

    List<ChatChannelMember> findByChannelIdAndStatusIn(Long channelId, Collection<ChannelMemberStatus> statuses);

    List<ChatChannelMember> findByAccountIdAndChannelIdIn(Long accountId, Collection<Long> channelIds);
}
