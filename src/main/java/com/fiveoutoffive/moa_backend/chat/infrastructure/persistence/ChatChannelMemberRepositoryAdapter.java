package com.fiveoutoffive.moa_backend.chat.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChatChannelMember;
import com.fiveoutoffive.moa_backend.chat.domain.repository.ChatChannelMemberRepository;
import com.fiveoutoffive.moa_backend.chat.infrastructure.mapper.ChatChannelMemberMapper;
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
}
