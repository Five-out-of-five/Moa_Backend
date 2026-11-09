package com.fiveoutoffive.moa_backend.chat.application.command;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChannelType;
import java.util.List;

public record ChannelCreateCommand(
        String name,
        ChannelType channelType,
        List<Long> memberAccountIds
) {
}
