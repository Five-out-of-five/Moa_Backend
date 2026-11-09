package com.fiveoutoffive.moa_backend.vote.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.vote.application.result.VoteDetailResponse;
import com.fiveoutoffive.moa_backend.vote.application.result.VoteSummaryResponse;
import com.fiveoutoffive.moa_backend.vote.application.usecase.VoteUseCase;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import com.fiveoutoffive.moa_backend.vote.presentation.api.request.BallotRequest;
import com.fiveoutoffive.moa_backend.vote.presentation.api.request.RevoteRequest;
import com.fiveoutoffive.moa_backend.vote.presentation.api.request.VoteCreateRequest;
import com.fiveoutoffive.moa_backend.vote.presentation.api.request.VoteUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/votes")
@RequiredArgsConstructor
@Tag(name = "10. Vote", description = "투표")
public class VoteController {

    private final VoteUseCase useCase;

    @GetMapping
    @Operation(summary = "투표 목록")
    public ApiResponse<PageResponse<VoteSummaryResponse>> list(@LoginAccount Long accountId, @PathVariable Long roomId, @RequestParam(required = false) VoteStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(useCase.getVotes(accountId, roomId, status, Pages.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PostMapping
    @Operation(summary = "투표 생성")
    public ApiResponse<VoteDetailResponse> create(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody VoteCreateRequest request) {
        return ApiResponse.success(useCase.create(accountId, roomId, request.toCommand()));
    }

    @GetMapping("/{voteId}")
    @Operation(summary = "투표 상세")
    public ApiResponse<VoteDetailResponse> detail(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long voteId) {
        return ApiResponse.success(useCase.getDetail(accountId, roomId, voteId));
    }

    @PatchMapping("/{voteId}")
    @Operation(summary = "투표 수정")
    public ApiResponse<VoteDetailResponse> update(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long voteId, @Valid @RequestBody VoteUpdateRequest request) {
        return ApiResponse.success(useCase.update(accountId, roomId, voteId, request.toCommand()));
    }

    @DeleteMapping("/{voteId}")
    @Operation(summary = "투표 삭제")
    public ApiResponse<Void> delete(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long voteId) {
        useCase.delete(accountId, roomId, voteId);
        return ApiResponse.empty();
    }

    @PutMapping("/{voteId}/ballots/me")
    @Operation(summary = "투표 참여·선택 변경")
    public ApiResponse<VoteDetailResponse> cast(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long voteId, @Valid @RequestBody BallotRequest request) {
        return ApiResponse.success(useCase.cast(accountId, roomId, voteId, request.toCommand()));
    }

    @PostMapping("/{voteId}/close")
    @Operation(summary = "투표 수동 마감")
    public ApiResponse<VoteDetailResponse> close(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long voteId) {
        return ApiResponse.success(useCase.close(accountId, roomId, voteId));
    }

    @PostMapping("/{voteId}/revote")
    @Operation(summary = "재투표")
    public ApiResponse<VoteDetailResponse> revote(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long voteId, @Valid @RequestBody RevoteRequest request) {
        return ApiResponse.success(useCase.revote(accountId, roomId, voteId, request.toCommand()));
    }
}
