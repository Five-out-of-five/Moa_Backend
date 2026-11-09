package com.fiveoutoffive.moa_backend.notice.application.result;

import com.fiveoutoffive.moa_backend.notice.domain.model.NoticeType;
import java.time.LocalDateTime;

public record NoticeResponse(Long noticeId, NoticeType noticeType, String title, String content, LocalDateTime displayStartsAt, LocalDateTime displayEndsAt, LocalDateTime maintenanceStartsAt) {
}
