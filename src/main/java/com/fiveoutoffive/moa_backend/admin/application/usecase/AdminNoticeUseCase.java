package com.fiveoutoffive.moa_backend.admin.application.usecase;

import com.fiveoutoffive.moa_backend.admin.application.command.NoticeCommand;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminNoticeResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.notice.application.result.NoticeResponse;
import com.fiveoutoffive.moa_backend.notice.domain.model.NoticeType;
import org.springframework.data.domain.Pageable;

public interface AdminNoticeUseCase {

    PageResponse<AdminNoticeResponse> getNotices(NoticeType noticeType, String state, Pageable pageable);

    NoticeResponse create(Long adminId, NoticeCommand command);

    NoticeResponse getNotice(Long noticeId);

    NoticeResponse update(Long adminId, Long noticeId, NoticeCommand command);

    void delete(Long adminId, Long noticeId);
}
