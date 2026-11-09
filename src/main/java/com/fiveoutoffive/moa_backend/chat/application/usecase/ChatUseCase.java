package com.fiveoutoffive.moa_backend.chat.application.usecase;

import com.fiveoutoffive.moa_backend.chat.application.command.ChannelCreateCommand;
import com.fiveoutoffive.moa_backend.chat.application.command.ChannelDeleteCommand;
import com.fiveoutoffive.moa_backend.chat.application.command.ChannelMemberAddCommand;
import com.fiveoutoffive.moa_backend.chat.application.command.ChannelUpdateCommand;
import com.fiveoutoffive.moa_backend.chat.application.command.DirectChannelCommand;
import com.fiveoutoffive.moa_backend.chat.application.command.MessageSendCommand;
import com.fiveoutoffive.moa_backend.chat.application.command.MessageUpdateCommand;
import com.fiveoutoffive.moa_backend.chat.application.command.ReadCommand;
import com.fiveoutoffive.moa_backend.chat.application.result.ChannelMemberResponse;
import com.fiveoutoffive.moa_backend.chat.application.result.ChannelResponse;
import com.fiveoutoffive.moa_backend.chat.application.result.MessageResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.CursorResponse;
import com.fiveoutoffive.moa_backend.room.domain.event.RoomCreatedEvent;
import com.fiveoutoffive.moa_backend.room.domain.event.RoomMemberJoinedEvent;
import com.fiveoutoffive.moa_backend.room.domain.event.RoomMemberLeftEvent;
import java.util.List;

public interface ChatUseCase {

    List<ChannelResponse> getChannels(Long accountId, Long roomId);

    ChannelResponse create(Long accountId, Long roomId, ChannelCreateCommand command);

    ChannelResponse getChannel(Long accountId, Long roomId, Long channelId);

    ChannelResponse update(Long accountId, Long roomId, Long channelId, ChannelUpdateCommand command);

    void delete(Long accountId, Long roomId, Long channelId, ChannelDeleteCommand command);

    ChannelResponse joinPublic(Long accountId, Long roomId, Long channelId);

    void leave(Long accountId, Long roomId, Long channelId);

    ChannelResponse acceptInvitation(Long accountId, Long roomId, Long channelId);

    List<ChannelMemberResponse> getMembers(Long accountId, Long roomId, Long channelId);

    List<ChannelMemberResponse> addMembers(Long accountId, Long roomId, Long channelId, ChannelMemberAddCommand command);

    void kickMember(Long accountId, Long roomId, Long channelId, Long targetId);

    ChannelResponse openDirect(Long accountId, Long roomId, DirectChannelCommand command);

    void hideDirect(Long accountId, Long roomId, Long channelId);

    CursorResponse<MessageResponse> getMessages(Long accountId, Long roomId, Long channelId, Long before, Long after, int size);

    MessageResponse send(Long accountId, Long roomId, Long channelId, MessageSendCommand command);

    MessageResponse updateMessage(Long accountId, Long roomId, Long channelId, Long messageId, MessageUpdateCommand command);

    void deleteMessage(Long accountId, Long roomId, Long channelId, Long messageId);

    void markRead(Long accountId, Long roomId, Long channelId, ReadCommand command);

    void onRoomCreated(RoomCreatedEvent event);

    void onMemberJoined(RoomMemberJoinedEvent event);

    void onMemberLeft(RoomMemberLeftEvent event);
}
