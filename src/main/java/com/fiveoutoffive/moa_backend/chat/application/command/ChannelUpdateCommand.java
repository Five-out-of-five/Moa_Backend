package com.fiveoutoffive.moa_backend.chat.application.command;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChannelType;

public record ChannelUpdateCommand(
        String name,
        ChannelType channelType
) {
}
