package com.fiveoutoffive.moa_backend.ideacard.application.result;

import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import java.time.LocalDateTime;

public record CommentResponse(Long commentId, MemberRef writer, String content, boolean deleted, boolean edited, LocalDateTime createdAt) {
}
