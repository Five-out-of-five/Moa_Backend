package com.fiveoutoffive.moa_backend.notice.application.result;

import com.fiveoutoffive.moa_backend.notice.domain.model.NoticeType;
import java.time.LocalDateTime;
import java.util.List;

public record ActiveNoticeResponse(Banner banner, List<Item> notices) {

    public record Banner(Long noticeId, NoticeType noticeType, String title) {
    }

    public record Item(Long noticeId, NoticeType noticeType, String title, LocalDateTime displayEndsAt) {
    }
}
