package com.fiveoutoffive.moa_backend.chat.infrastructure.adapter;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChannelMemberStatus;
import com.fiveoutoffive.moa_backend.chat.domain.repository.ChatChannelMemberRepository;
import com.fiveoutoffive.moa_backend.global.websocket.StompDestinationAuthorizer;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 구독·전송 경로 권한: /topic/rooms/{id}, /app/rooms/{id} 는 방 멤버, /topic/channels/{id}, /app/channels/{id} 는 채널 참여자. */
@Component
@RequiredArgsConstructor
public class ChatDestinationAuthorizer implements StompDestinationAuthorizer {

    private static final Pattern ROOM = Pattern.compile("^/(?:topic|app)/rooms/(\\d+)(?:/.*)?$");
    private static final Pattern CHANNEL = Pattern.compile("^/(?:topic|app)/channels/(\\d+)(?:/.*)?$");

    private final RoomAccessService access;
    private final ChatChannelMemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean canAccess(Long userId, String destination) {
        if (destination.startsWith("/user/")) {
            return true;
        }
        Matcher room = ROOM.matcher(destination);
        if (room.matches()) {
            return access.isActiveMember(Long.valueOf(room.group(1)), userId);
        }
        Matcher channel = CHANNEL.matcher(destination);
        if (channel.matches()) {
            return memberRepository.findByChannelIdAndAccountId(Long.valueOf(channel.group(1)), userId).filter(m -> m.getStatus() == ChannelMemberStatus.JOINED).isPresent();
        }
        return false;
    }
}
