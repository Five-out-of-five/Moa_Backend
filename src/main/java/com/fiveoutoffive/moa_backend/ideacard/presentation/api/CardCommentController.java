package com.fiveoutoffive.moa_backend.ideacard.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.CommentResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.usecase.IdeaCardUseCase;
import com.fiveoutoffive.moa_backend.ideacard.presentation.api.request.CommentCreateRequest;
import com.fiveoutoffive.moa_backend.ideacard.presentation.api.request.CommentUpdateRequest;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/idea-cards/{cardId}/comments")
@RequiredArgsConstructor
@Tag(name = "08. IdeaCard", description = "아이디어 카드")
public class CardCommentController {

    private final IdeaCardUseCase useCase;

    @GetMapping
    @Operation(summary = "댓글 목록")
    public ApiResponse<List<CommentResponse>> list(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long cardId) {
        return ApiResponse.success(useCase.getComments(accountId, roomId, cardId));
    }

    @PostMapping
    @Operation(summary = "댓글 작성")
    public ApiResponse<CommentResponse> add(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long cardId, @Valid @RequestBody CommentCreateRequest request) {
        return ApiResponse.success(useCase.addComment(accountId, roomId, cardId, request.toCommand()));
    }

    @PatchMapping("/{commentId}")
    @Operation(summary = "댓글 수정")
    public ApiResponse<CommentResponse> update(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long cardId, @PathVariable Long commentId, @Valid @RequestBody CommentUpdateRequest request) {
        return ApiResponse.success(useCase.updateComment(accountId, roomId, cardId, commentId, request.toCommand()));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제")
    public ApiResponse<Void> delete(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long cardId, @PathVariable Long commentId) {
        useCase.deleteComment(accountId, roomId, cardId, commentId);
        return ApiResponse.empty();
    }
}
