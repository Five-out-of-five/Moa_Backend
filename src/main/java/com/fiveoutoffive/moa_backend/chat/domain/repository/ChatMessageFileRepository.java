package com.fiveoutoffive.moa_backend.chat.domain.repository;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChatMessageFile;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface ChatMessageFileRepository {

    ChatMessageFile save(ChatMessageFile chatMessageFile);

    Optional<ChatMessageFile> findById(Long id);

    List<ChatMessageFile> findByMessageIdInOrderBySortOrderAsc(Collection<Long> messageIds);
}
