package com.fiveoutoffive.moa_backend.chat.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChatChannel;
import com.fiveoutoffive.moa_backend.chat.domain.repository.ChatChannelRepository;
import com.fiveoutoffive.moa_backend.chat.infrastructure.mapper.ChatChannelMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatChannelRepositoryAdapter implements ChatChannelRepository {

    private final SpringDataChatChannelRepository springDataRepository;
    private final ChatChannelMapper chatChannelMapper;

    @Override
    public ChatChannel save(ChatChannel chatChannel) {
        ChatChannelJpaEntity saved = springDataRepository.save(chatChannelMapper.toJpaEntity(chatChannel));
        return chatChannelMapper.toDomain(saved);
    }

    @Override
    public Optional<ChatChannel> findById(Long id) {
        return springDataRepository.findById(id)
                .map(chatChannelMapper::toDomain);
    }
}
