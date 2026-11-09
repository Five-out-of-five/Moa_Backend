package com.fiveoutoffive.moa_backend.chat.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChatMessageFile;
import com.fiveoutoffive.moa_backend.chat.domain.repository.ChatMessageFileRepository;
import com.fiveoutoffive.moa_backend.chat.infrastructure.mapper.ChatMessageFileMapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatMessageFileRepositoryAdapter implements ChatMessageFileRepository {

    private final SpringDataChatMessageFileRepository springDataRepository;
    private final ChatMessageFileMapper chatMessageFileMapper;

    @Override
    public ChatMessageFile save(ChatMessageFile chatMessageFile) {
        ChatMessageFileJpaEntity saved = springDataRepository.save(chatMessageFileMapper.toJpaEntity(chatMessageFile));
        return chatMessageFileMapper.toDomain(saved);
    }

    @Override
    public Optional<ChatMessageFile> findById(Long id) {
        return springDataRepository.findById(id)
                .map(chatMessageFileMapper::toDomain);
    }

    @Override
    public List<ChatMessageFile> findByMessageIdInOrderBySortOrderAsc(Collection<Long> messageIds) {
        return springDataRepository.findByMessageIdInOrderBySortOrderAsc(messageIds).stream().map(chatMessageFileMapper::toDomain).toList();
    }
}
