package com.fiveoutoffive.moa_backend.room.infrastructure.adapter;

import com.fiveoutoffive.moa_backend.global.websocket.PresenceSubscriberPort;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMember;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberStatus;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomMemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RoomPresenceSubscriberAdapter implements PresenceSubscriberPort {

    private final RoomMemberRepository roomMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Long> findRoomIds(Long userId) {
        return roomMemberRepository.findByAccountIdAndStatus(userId, RoomMemberStatus.ACTIVE).stream().map(RoomMember::getRoomId).toList();
    }
}
