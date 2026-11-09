package com.fiveoutoffive.moa_backend.chat.presentation.api.request;

import com.fiveoutoffive.moa_backend.chat.application.command.ChannelCreateCommand;
import com.fiveoutoffive.moa_backend.chat.domain.model.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ChannelCreateRequest(
        @NotBlank @Size(max=30) String name,
        @NotNull ChannelType channelType,
        List<Long> memberAccountIds
) {

    public ChannelCreateCommand toCommand() {
        return new ChannelCreateCommand(name, channelType, memberAccountIds);
    }
}
