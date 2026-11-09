package com.fiveoutoffive.moa_backend.chat.application.command;

import java.util.List;

public record MessageSendCommand(
        String content,
        List<Long> fileIds,
        String clientMessageId
) {
}
