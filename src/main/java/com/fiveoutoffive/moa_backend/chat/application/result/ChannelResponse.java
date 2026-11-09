package com.fiveoutoffive.moa_backend.chat.application.result;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChannelType;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;

public record ChannelResponse(Long channelId, Long roomId, ChannelType channelType, String name, Boolean isDefault, Long creatorAccountId, String myStatus, long unreadCount, MemberRef peer) {
}
