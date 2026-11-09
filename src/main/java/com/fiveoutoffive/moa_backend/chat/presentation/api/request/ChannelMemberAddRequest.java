package com.fiveoutoffive.moa_backend.chat.presentation.api.request;

import com.fiveoutoffive.moa_backend.chat.application.command.ChannelMemberAddCommand;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ChannelMemberAddRequest(
        @NotEmpty List<Long> accountIds
) {

    public ChannelMemberAddCommand toCommand() {
        return new ChannelMemberAddCommand(accountIds);
    }
}
