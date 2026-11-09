package com.fiveoutoffive.moa_backend.chat.application.result;

public record MyChannelEvent(String type, Long channelId, Long roomId, Long unreadCount) {
}
