package com.fiveoutoffive.moa_backend.ai.presentation.api.request;

import com.fiveoutoffive.moa_backend.ai.application.command.ToneCheckCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ToneCheckRequest(
        @NotBlank @Size(max=2000) String text,
        // API 명세: target = IDEA_CARD / CHAT_MESSAGE
        @NotBlank @Pattern(regexp = "IDEA_CARD|CHAT_MESSAGE") String target,
        Long channelId
) {

    public ToneCheckCommand toCommand() {
        return new ToneCheckCommand(text, target, channelId);
    }
}
