package com.fiveoutoffive.moa_backend.chat.application.result;

import com.fiveoutoffive.moa_backend.chat.domain.model.MessageType;
import com.fiveoutoffive.moa_backend.file.application.result.FileResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import java.time.LocalDateTime;
import java.util.List;

public record MessageResponse(Long messageId, Long channelId, MessageType messageType, MemberRef sender, String content, List<FileResponse> files, boolean edited, boolean deleted, LocalDateTime createdAt, String clientMessageId) {
}
