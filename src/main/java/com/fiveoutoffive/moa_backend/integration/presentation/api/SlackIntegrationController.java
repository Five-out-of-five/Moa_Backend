package com.fiveoutoffive.moa_backend.integration.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.integration.application.result.SlackDeliveryResponse;
import com.fiveoutoffive.moa_backend.integration.application.result.SlackIntegrationResponse;
import com.fiveoutoffive.moa_backend.integration.application.usecase.SlackIntegrationUseCase;
import com.fiveoutoffive.moa_backend.integration.domain.model.SlackDeliveryStatus;
import com.fiveoutoffive.moa_backend.integration.presentation.api.request.SlackIntegrationRequest;
import com.fiveoutoffive.moa_backend.integration.presentation.api.request.SlackTestRequest;
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
@RequestMapping("/api/v1/rooms/{roomId}/integrations/slack")
@RequiredArgsConstructor
@Tag(name = "16. Integration", description = "외부 연동")
public class SlackIntegrationController {

    private final SlackIntegrationUseCase useCase;

    @GetMapping
    @Operation(summary = "슬랙 연동 조회")
    public ApiResponse<SlackIntegrationResponse> get(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.get(accountId, roomId));
    }

    @PostMapping("/test")
    @Operation(summary = "슬랙 테스트 발송")
    public ApiResponse<Void> test(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody SlackTestRequest request) {
        useCase.test(accountId, roomId, request.toCommand());
        return ApiResponse.empty();
    }

    @PutMapping
    @Operation(summary = "슬랙 연동 등록·수정")
    public ApiResponse<SlackIntegrationResponse> upsert(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody SlackIntegrationRequest request) {
        return ApiResponse.success(useCase.upsert(accountId, roomId, request.toCommand()));
    }

    @DeleteMapping
    @Operation(summary = "슬랙 연동 해제")
    public ApiResponse<Void> delete(@LoginAccount Long accountId, @PathVariable Long roomId) {
        useCase.delete(accountId, roomId);
        return ApiResponse.empty();
    }

    @GetMapping("/deliveries")
    @Operation(summary = "슬랙 발송 이력")
    public ApiResponse<PageResponse<SlackDeliveryResponse>> deliveries(@LoginAccount Long accountId, @PathVariable Long roomId, @RequestParam(required = false) SlackDeliveryStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(useCase.getDeliveries(accountId, roomId, status, Pages.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }
}
