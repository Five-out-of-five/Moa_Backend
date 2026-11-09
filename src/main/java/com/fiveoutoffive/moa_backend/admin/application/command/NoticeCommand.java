package com.fiveoutoffive.moa_backend.admin.application.command;

import com.fiveoutoffive.moa_backend.notice.domain.model.NoticeType;
import java.time.OffsetDateTime;

public record NoticeCommand(
        NoticeType noticeType,
        String title,
        String content,
        OffsetDateTime displayStartsAt,
        OffsetDateTime displayEndsAt,
        OffsetDateTime maintenanceStartsAt
) {
}
