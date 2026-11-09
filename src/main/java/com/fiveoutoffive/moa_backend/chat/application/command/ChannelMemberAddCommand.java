package com.fiveoutoffive.moa_backend.chat.application.command;

import java.util.List;

public record ChannelMemberAddCommand(
        List<Long> accountIds
) {
}
