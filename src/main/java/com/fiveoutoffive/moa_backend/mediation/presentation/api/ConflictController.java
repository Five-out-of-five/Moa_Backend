package com.fiveoutoffive.moa_backend.mediation.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.IdeaCardDetailResponse;
import com.fiveoutoffive.moa_backend.mediation.application.result.CompromiseDraftResponse;
import com.fiveoutoffive.moa_backend.mediation.application.result.ConflictDetailResponse;
import com.fiveoutoffive.moa_backend.mediation.application.result.ConflictSummaryResponse;
import com.fiveoutoffive.moa_backend.mediation.application.usecase.MediationUseCase;
import com.fiveoutoffive.moa_backend.mediation.domain.model.ConflictStatus;
import com.fiveoutoffive.moa_backend.mediation.presentation.api.request.DraftAdoptRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/conflicts")
@RequiredArgsConstructor
@Tag(name = "12. Mediation", description = "중재")
public class ConflictController {

    private final MediationUseCase useCase;

    @GetMapping
    @Operation(summary = "충돌 목록 (중재 위젯)")
    public ApiResponse<List<ConflictSummaryResponse>> list(@LoginAccount Long accountId, @PathVariable Long roomId, @RequestParam(required = false) ConflictStatus status) {
        return ApiResponse.success(useCase.getConflicts(accountId, roomId, status));
    }

    @GetMapping("/{conflictId}")
    @Operation(summary = "충돌 상세 (두 카드 비교)")
    public ApiResponse<ConflictDetailResponse> detail(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long conflictId) {
        return ApiResponse.success(useCase.getConflict(accountId, roomId, conflictId));
    }

    @PostMapping("/{conflictId}/dismiss")
    @Operation(summary = "충돌 배지 해제")
    public ApiResponse<ConflictSummaryResponse> dismiss(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long conflictId) {
        return ApiResponse.success(useCase.dismiss(accountId, roomId, conflictId));
    }

    @PostMapping("/{conflictId}/compromise-drafts")
    @Operation(summary = "절충안 생성 (AI)")
    public ApiResponse<CompromiseDraftResponse> draft(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long conflictId) {
        return ApiResponse.success(useCase.createDraft(accountId, roomId, conflictId));
    }

    @PostMapping("/{conflictId}/compromise-drafts/{draftId}/adopt")
    @Operation(summary = "절충안 채택")
    public ApiResponse<IdeaCardDetailResponse> adopt(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long conflictId, @PathVariable Long draftId, @Valid @RequestBody DraftAdoptRequest request) {
        return ApiResponse.success(useCase.adopt(accountId, roomId, conflictId, draftId, request.toCommand()));
    }

    @PostMapping("/{conflictId}/compromise-drafts/{draftId}/discard")
    @Operation(summary = "절충안 폐기")
    public ApiResponse<ConflictSummaryResponse> discard(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long conflictId, @PathVariable Long draftId) {
        return ApiResponse.success(useCase.discard(accountId, roomId, conflictId, draftId));
    }
}
