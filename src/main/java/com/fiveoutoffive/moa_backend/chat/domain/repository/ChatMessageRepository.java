package com.fiveoutoffive.moa_backend.chat.domain.repository;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChatMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface ChatMessageRepository {

    ChatMessage save(ChatMessage chatMessage);

    Optional<ChatMessage> findById(Long id);

    List<ChatMessage> findBefore(@Param("channelId") Long channelId, @Param("before") Long before, @Param("limit") int limit);

    List<ChatMessage> findAfter(@Param("channelId") Long channelId, @Param("after") Long after, @Param("limit") int limit);

    long countUnread(@Param("channelId") Long channelId, @Param("lastRead") long lastRead, @Param("me") Long me);

    /** 채널의 삭제되지 않은 메시지를 모두 삭제 처리한다. (채널 삭제) */
    int softDeleteByChannelId(Long channelId, LocalDateTime deletedAt);

    long countByDeletedAtIsNull();

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from, LocalDateTime to);
}
