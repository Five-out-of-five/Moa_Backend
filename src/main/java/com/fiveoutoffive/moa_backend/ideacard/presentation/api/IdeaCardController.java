package com.fiveoutoffive.moa_backend.ideacard.presentation.api;

import com.fiveoutoffive.moa_backend.file.application.result.FileResponse;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.EditLockResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.IdeaCardDetailResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.IdeaCardSummaryResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.usecase.IdeaCardUseCase;
import com.fiveoutoffive.moa_backend.ideacard.presentation.api.request.AttachFileRequest;
import com.fiveoutoffive.moa_backend.ideacard.presentation.api.request.CardMoveRequest;
import com.fiveoutoffive.moa_backend.ideacard.presentation.api.request.IdeaCardCreateRequest;
import com.fiveoutoffive.moa_backend.ideacard.presentation.api.request.IdeaCardUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/idea-cards")
@RequiredArgsConstructor
@Tag(name = "08. IdeaCard", description = "아이디어 카드")
public class IdeaCardController {

    private final IdeaCardUseCase useCase;

    @GetMapping
    @Operation(summary = "아이디어 보드 카드 목록")
    public ApiResponse<List<IdeaCardSummaryResponse>> list(@LoginAccount Long accountId, @PathVariable Long roomId, @RequestParam(required = false) Long sessionId) {
        return ApiResponse.success(useCase.getCards(accountId, roomId, sessionId));
    }

    @PostMapping
    @Operation(summary = "아이디어 카드 등록")
    public ApiResponse<IdeaCardDetailResponse> create(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody IdeaCardCreateRequest request) {
        return ApiResponse.success(useCase.create(accountId, roomId, request.toCommand()));
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "아이디어 카드 상세")
    public ApiResponse<IdeaCardDetailResponse> detail(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long cardId) {
        return ApiResponse.success(useCase.getDetail(accountId, roomId, cardId));
    }

    @PatchMapping("/{cardId}")
    @Operation(summary = "아이디어 카드 수정")
    public ApiResponse<IdeaCardDetailResponse> update(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long cardId, @Valid @RequestBody IdeaCardUpdateRequest request) {
        return ApiResponse.success(useCase.update(accountId, roomId, cardId, request.toCommand()));
    }

    @PostMapping("/{cardId}/edit-lock")
    @Operation(summary = "카드 편집 잠금")
    public ApiResponse<EditLockResponse> lock(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long cardId) {
        return ApiResponse.success(useCase.lock(accountId, roomId, cardId));
    }

    @DeleteMapping("/{cardId}/edit-lock")
    @Operation(summary = "카드 편집 잠금 해제")
    public ApiResponse<Void> unlock(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long cardId) {
        useCase.unlock(accountId, roomId, cardId);
        return ApiResponse.empty();
    }

    @DeleteMapping("/{cardId}")
    @Operation(summary = "아이디어 카드 삭제")
    public ApiResponse<Void> delete(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long cardId) {
        useCase.delete(accountId, roomId, cardId);
        return ApiResponse.empty();
    }

    @PatchMapping("/{cardId}/position")
    @Operation(summary = "카드 이동·정렬")
    public ApiResponse<List<IdeaCardSummaryResponse>> move(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long cardId, @Valid @RequestBody CardMoveRequest request) {
        return ApiResponse.success(useCase.move(accountId, roomId, cardId, request.toCommand()));
    }

    @PostMapping("/{cardId}/files")
    @Operation(summary = "카드 첨부 추가")
    public ApiResponse<List<FileResponse>> attach(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long cardId, @Valid @RequestBody AttachFileRequest request) {
        return ApiResponse.success(useCase.attachFile(accountId, roomId, cardId, request.toCommand()));
    }

    @DeleteMapping("/{cardId}/files/{fileId}")
    @Operation(summary = "카드 첨부 삭제")
    public ApiResponse<Void> removeFile(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long cardId, @PathVariable Long fileId) {
        useCase.removeFile(accountId, roomId, cardId, fileId);
        return ApiResponse.empty();
    }
}
