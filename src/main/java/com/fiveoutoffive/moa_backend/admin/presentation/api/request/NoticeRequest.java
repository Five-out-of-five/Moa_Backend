package com.fiveoutoffive.moa_backend.admin.presentation.api.request;

import com.fiveoutoffive.moa_backend.admin.application.command.NoticeCommand;
import com.fiveoutoffive.moa_backend.notice.domain.model.NoticeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record NoticeRequest(
        @NotNull NoticeType noticeType,
        @NotBlank @Size(max=200) String title,
        @NotBlank String content,
        @NotNull OffsetDateTime displayStartsAt,
        @NotNull OffsetDateTime displayEndsAt,
        OffsetDateTime maintenanceStartsAt
) {

    public NoticeCommand toCommand() {
        return new NoticeCommand(noticeType, title, content, displayStartsAt, displayEndsAt, maintenanceStartsAt);
    }
}
