package com.fiveoutoffive.moa_backend.admin.presentation.api;

import com.fiveoutoffive.moa_backend.admin.application.result.AdminNoticeResponse;
import com.fiveoutoffive.moa_backend.admin.application.usecase.AdminNoticeUseCase;
import com.fiveoutoffive.moa_backend.admin.presentation.api.request.NoticeRequest;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.notice.application.result.NoticeResponse;
import com.fiveoutoffive.moa_backend.notice.domain.model.NoticeType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/notices")
@RequiredArgsConstructor
@Tag(name = "19. Admin", description = "서비스 관리자")
public class AdminNoticeController {

    private final AdminNoticeUseCase useCase;

    @GetMapping
    @Operation(summary = "[관리자] 공지 목록")
    public ApiResponse<PageResponse<AdminNoticeResponse>> list(@RequestParam(required = false) NoticeType noticeType, @RequestParam(required = false) String state, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(useCase.getNotices(noticeType, state, Pages.of(page, size, Sort.by(Sort.Direction.DESC, "displayStartsAt"))));
    }

    @PostMapping
    @Operation(summary = "[관리자] 공지 등록")
    public ApiResponse<NoticeResponse> create(@LoginAccount Long adminId, @Valid @RequestBody NoticeRequest request) {
        return ApiResponse.success(useCase.create(adminId, request.toCommand()));
    }

    @GetMapping("/{noticeId}")
    @Operation(summary = "[관리자] 공지 상세")
    public ApiResponse<NoticeResponse> detail(@PathVariable Long noticeId) {
        return ApiResponse.success(useCase.getNotice(noticeId));
    }

    @PutMapping("/{noticeId}")
    @Operation(summary = "[관리자] 공지 수정")
    public ApiResponse<NoticeResponse> update(@LoginAccount Long adminId, @PathVariable Long noticeId, @Valid @RequestBody NoticeRequest request) {
        return ApiResponse.success(useCase.update(adminId, noticeId, request.toCommand()));
    }

    @DeleteMapping("/{noticeId}")
    @Operation(summary = "[관리자] 공지 삭제")
    public ApiResponse<Void> delete(@LoginAccount Long adminId, @PathVariable Long noticeId) {
        useCase.delete(adminId, noticeId);
        return ApiResponse.empty();
    }
}
