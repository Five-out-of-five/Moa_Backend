package com.fiveoutoffive.moa_backend.ai.application.command;

public record ToneCheckCommand(
        String text,
        String target,
        Long channelId
) {
}
