package com.fiveoutoffive.moa_backend.chat.presentation.api.request;

import com.fiveoutoffive.moa_backend.chat.application.command.ChannelUpdateCommand;
import com.fiveoutoffive.moa_backend.chat.domain.model.ChannelType;
import jakarta.validation.constraints.Size;

public record ChannelUpdateRequest(
        @Size(min=1,max=30) String name,
        ChannelType channelType
) {

    public ChannelUpdateCommand toCommand() {
        return new ChannelUpdateCommand(name, channelType);
    }
}
