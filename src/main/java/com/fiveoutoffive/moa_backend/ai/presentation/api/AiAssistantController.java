package com.fiveoutoffive.moa_backend.ai.presentation.api;

import com.fiveoutoffive.moa_backend.ai.application.result.AssistantMessageResponse;
import com.fiveoutoffive.moa_backend.ai.application.usecase.AiUseCase;
import com.fiveoutoffive.moa_backend.ai.presentation.api.request.AssistantAskRequest;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.CursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/ai/assistant/messages")
@RequiredArgsConstructor
@Tag(name = "15. AI", description = "AI")
public class AiAssistantController {

    private final AiUseCase useCase;

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI 어시스턴트 질문 (스트리밍)")
    public SseEmitter ask(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody AssistantAskRequest request) {
        return useCase.ask(accountId, roomId, request.toCommand());
    }

    @GetMapping
    @Operation(summary = "AI 어시스턴트 대화 이력")
    public ApiResponse<CursorResponse<AssistantMessageResponse>> history(@LoginAccount Long accountId, @PathVariable Long roomId, @RequestParam(required = false) Long before, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(useCase.getHistory(accountId, roomId, before, size));
    }
}
