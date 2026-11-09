package com.fiveoutoffive.moa_backend.chat.domain.repository;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChatChannel;
import java.util.List;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface ChatChannelRepository {

    ChatChannel save(ChatChannel chatChannel);

    Optional<ChatChannel> findById(Long id);

    List<ChatChannel> findByRoomIdAndDeletedAtIsNull(Long roomId);

    Optional<ChatChannel> findFirstByRoomIdAndIsDefaultTrue(Long roomId);

    Optional<ChatChannel> findByRoomIdAndDmKey(Long roomId, String dmKey);

    boolean existsByRoomIdAndNameAndDeletedAtIsNull(Long roomId, String name);
}
