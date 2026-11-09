package com.fiveoutoffive.moa_backend.ideacard.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.ReactionDetailResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.ReactionSummaryResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.usecase.IdeaCardUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/idea-cards/{cardId}/reactions")
@RequiredArgsConstructor
@Tag(name = "08. IdeaCard", description = "아이디어 카드")
public class CardReactionController {

    private final IdeaCardUseCase useCase;

    @PutMapping("/{emoji}")
    @Operation(summary = "리액션 추가")
    public ApiResponse<List<ReactionSummaryResponse>> add(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long cardId, @PathVariable String emoji) {
        return ApiResponse.success(useCase.addReaction(accountId, roomId, cardId, emoji));
    }

    @GetMapping
    @Operation(summary = "리액션한 사용자 목록")
    public ApiResponse<List<ReactionDetailResponse>> list(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long cardId) {
        return ApiResponse.success(useCase.getReactions(accountId, roomId, cardId));
    }

    @DeleteMapping("/{emoji}")
    @Operation(summary = "리액션 취소")
    public ApiResponse<List<ReactionSummaryResponse>> remove(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long cardId, @PathVariable String emoji) {
        return ApiResponse.success(useCase.removeReaction(accountId, roomId, cardId, emoji));
    }
}
