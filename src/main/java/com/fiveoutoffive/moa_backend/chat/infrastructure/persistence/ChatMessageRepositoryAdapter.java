package com.fiveoutoffive.moa_backend.chat.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChatMessage;
import com.fiveoutoffive.moa_backend.chat.domain.repository.ChatMessageRepository;
import com.fiveoutoffive.moa_backend.chat.infrastructure.mapper.ChatMessageMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
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

    @Override
    public List<ChatMessage> findBefore(@Param("channelId") Long channelId, @Param("before") Long before, @Param("limit") int limit) {
        return springDataRepository.findBefore(channelId, before, limit).stream().map(chatMessageMapper::toDomain).toList();
    }

    @Override
    public List<ChatMessage> findAfter(@Param("channelId") Long channelId, @Param("after") Long after, @Param("limit") int limit) {
        return springDataRepository.findAfter(channelId, after, limit).stream().map(chatMessageMapper::toDomain).toList();
    }

    @Override
    public long countUnread(@Param("channelId") Long channelId, @Param("lastRead") long lastRead, @Param("me") Long me) {
        return springDataRepository.countUnread(channelId, lastRead, me);
    }

    @Override
    public long countByDeletedAtIsNull() {
        return springDataRepository.countByDeletedAtIsNull();
    }

    @Override
    public long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from, LocalDateTime to) {
        return springDataRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(from, to);
    }
}
