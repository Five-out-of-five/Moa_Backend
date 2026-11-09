package com.fiveoutoffive.moa_backend.ai.presentation.api.request;

import com.fiveoutoffive.moa_backend.ai.application.command.ToneCheckCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ToneCheckRequest(
        @NotBlank @Size(max=2000) String text,
        @NotBlank String target,
        Long channelId
) {

    public ToneCheckCommand toCommand() {
        return new ToneCheckCommand(text, target, channelId);
    }
}
