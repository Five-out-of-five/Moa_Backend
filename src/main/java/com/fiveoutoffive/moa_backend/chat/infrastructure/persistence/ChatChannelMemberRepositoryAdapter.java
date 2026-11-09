package com.fiveoutoffive.moa_backend.chat.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChannelMemberStatus;
import com.fiveoutoffive.moa_backend.chat.domain.model.ChatChannelMember;
import com.fiveoutoffive.moa_backend.chat.domain.repository.ChatChannelMemberRepository;
import com.fiveoutoffive.moa_backend.chat.infrastructure.mapper.ChatChannelMemberMapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatChannelMemberRepositoryAdapter implements ChatChannelMemberRepository {

    private final SpringDataChatChannelMemberRepository springDataRepository;
    private final ChatChannelMemberMapper chatChannelMemberMapper;

    @Override
    public ChatChannelMember save(ChatChannelMember chatChannelMember) {
        ChatChannelMemberJpaEntity saved = springDataRepository.save(chatChannelMemberMapper.toJpaEntity(chatChannelMember));
        return chatChannelMemberMapper.toDomain(saved);
    }

    @Override
    public Optional<ChatChannelMember> findById(Long id) {
        return springDataRepository.findById(id)
                .map(chatChannelMemberMapper::toDomain);
    }

    @Override
    public Optional<ChatChannelMember> findByChannelIdAndAccountId(Long channelId, Long accountId) {
        return springDataRepository.findByChannelIdAndAccountId(channelId, accountId).map(chatChannelMemberMapper::toDomain);
    }

    @Override
    public List<ChatChannelMember> findByChannelIdAndStatusIn(Long channelId, Collection<ChannelMemberStatus> statuses) {
        return springDataRepository.findByChannelIdAndStatusIn(channelId, statuses).stream().map(chatChannelMemberMapper::toDomain).toList();
    }

    @Override
    public List<ChatChannelMember> findByAccountIdAndChannelIdIn(Long accountId, Collection<Long> channelIds) {
        return springDataRepository.findByAccountIdAndChannelIdIn(accountId, channelIds).stream().map(chatChannelMemberMapper::toDomain).toList();
    }
}
