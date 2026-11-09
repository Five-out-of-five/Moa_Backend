package com.fiveoutoffive.moa_backend.notice.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.notice.application.result.ActiveNoticeResponse;
import com.fiveoutoffive.moa_backend.notice.application.result.NoticeResponse;
import com.fiveoutoffive.moa_backend.notice.application.result.NoticeSummaryResponse;
import com.fiveoutoffive.moa_backend.notice.application.usecase.NoticeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@Tag(name = "18. Notice", description = "공지")
public class NoticeController {

    private final NoticeUseCase useCase;

    @GetMapping("/active")
    @Operation(summary = "노출 중인 공지 (배너)")
    public ApiResponse<ActiveNoticeResponse> active(@LoginAccount Long accountId) {
        return ApiResponse.success(useCase.getActive(accountId));
    }

    @GetMapping
    @Operation(summary = "공지 목록")
    public ApiResponse<PageResponse<NoticeSummaryResponse>> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(useCase.getList(Pages.of(page, size, Sort.by(Sort.Direction.DESC, "displayStartsAt"))));
    }

    @GetMapping("/{noticeId:\\d+}")
    @Operation(summary = "공지 상세")
    public ApiResponse<NoticeResponse> detail(@PathVariable Long noticeId) {
        return ApiResponse.success(useCase.getDetail(noticeId));
    }

    @PostMapping("/{noticeId:\\d+}/dismiss")
    @Operation(summary = "공지 닫기")
    public ApiResponse<Void> dismiss(@LoginAccount Long accountId, @PathVariable Long noticeId) {
        useCase.dismiss(accountId, noticeId);
        return ApiResponse.empty();
    }
}
