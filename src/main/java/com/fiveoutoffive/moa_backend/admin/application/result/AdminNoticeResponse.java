package com.fiveoutoffive.moa_backend.admin.application.result;

import com.fiveoutoffive.moa_backend.notice.domain.model.NoticeType;
import java.time.LocalDateTime;

public record AdminNoticeResponse(Long noticeId, NoticeType noticeType, String title, LocalDateTime displayStartsAt, LocalDateTime displayEndsAt, String state, Author author) {

    public record Author(Long accountId, String name) {
    }
}
