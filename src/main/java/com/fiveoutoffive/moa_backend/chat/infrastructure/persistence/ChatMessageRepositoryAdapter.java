package com.fiveoutoffive.moa_backend.chat.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChatMessage;
import com.fiveoutoffive.moa_backend.chat.domain.repository.ChatMessageRepository;
import com.fiveoutoffive.moa_backend.chat.infrastructure.mapper.ChatMessageMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryAdapter implements ChatMessageRepository {

    private final SpringDataChatMessageRepository springDataRepository;
    private final ChatMessageMapper chatMessageMapper;

    @Override
    public ChatMessage save(ChatMessage chatMessage) {
        ChatMessageJpaEntity saved = springDataRepository.save(chatMessageMapper.toJpaEntity(chatMessage));
        return chatMessageMapper.toDomain(saved);
    }

    @Override
    public Optional<ChatMessage> findById(Long id) {
        return springDataRepository.findById(id)
                .map(chatMessageMapper::toDomain);
    }
}
