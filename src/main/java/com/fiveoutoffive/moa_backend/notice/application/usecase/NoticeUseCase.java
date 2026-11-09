package com.fiveoutoffive.moa_backend.notice.application.usecase;

import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.notice.application.result.ActiveNoticeResponse;
import com.fiveoutoffive.moa_backend.notice.application.result.NoticeResponse;
import com.fiveoutoffive.moa_backend.notice.application.result.NoticeSummaryResponse;
import org.springframework.data.domain.Pageable;

public interface NoticeUseCase {

    ActiveNoticeResponse getActive(Long accountId);

    PageResponse<NoticeSummaryResponse> getList(Pageable pageable);

    NoticeResponse getDetail(Long noticeId);

    void dismiss(Long accountId, Long noticeId);
}
