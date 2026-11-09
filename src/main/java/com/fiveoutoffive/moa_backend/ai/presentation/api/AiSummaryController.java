package com.fiveoutoffive.moa_backend.ai.presentation.api;

import com.fiveoutoffive.moa_backend.ai.application.result.SummaryResponse;
import com.fiveoutoffive.moa_backend.ai.application.usecase.AiUseCase;
import com.fiveoutoffive.moa_backend.ai.presentation.api.request.SummaryCreateRequest;
import com.fiveoutoffive.moa_backend.ai.presentation.api.request.SummaryToCardRequest;
import com.fiveoutoffive.moa_backend.ai.presentation.api.request.SummaryUpdateRequest;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.ideacard.application.result.IdeaCardDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/ai/summaries")
@RequiredArgsConstructor
@Tag(name = "15. AI", description = "AI")
public class AiSummaryController {

    private final AiUseCase useCase;

    @PostMapping
    @Operation(summary = "회의 요약 생성")
    public ApiResponse<SummaryResponse> create(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody SummaryCreateRequest request) {
        return ApiResponse.success(useCase.createSummary(accountId, roomId, request.toCommand()));
    }

    @GetMapping
    @Operation(summary = "회의 요약 목록")
    public ApiResponse<PageResponse<SummaryResponse>> list(@LoginAccount Long accountId, @PathVariable Long roomId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(useCase.getSummaries(accountId, roomId, Pages.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PatchMapping("/{summaryId}")
    @Operation(summary = "회의 요약 수정")
    public ApiResponse<SummaryResponse> update(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long summaryId, @Valid @RequestBody SummaryUpdateRequest request) {
        return ApiResponse.success(useCase.updateSummary(accountId, roomId, summaryId, request.toCommand()));
    }

    @PostMapping("/{summaryId}/save-as-card")
    @Operation(summary = "요약을 카드로 저장")
    public ApiResponse<IdeaCardDetailResponse> saveAsCard(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long summaryId, @Valid @RequestBody SummaryToCardRequest request) {
        return ApiResponse.success(useCase.saveSummaryAsCard(accountId, roomId, summaryId, request.toCommand()));
    }
}
