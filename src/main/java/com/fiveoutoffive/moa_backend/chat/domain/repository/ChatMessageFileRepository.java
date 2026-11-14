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

    /** 채널에 속한 메시지들의 첨부 파일 ID (채널 삭제 때 파일도 함께 지운다) */
    List<Long> findFileIdsByChannelId(Long channelId);
}
