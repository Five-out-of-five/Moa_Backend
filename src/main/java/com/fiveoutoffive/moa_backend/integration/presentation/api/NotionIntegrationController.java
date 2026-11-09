package com.fiveoutoffive.moa_backend.integration.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.integration.application.result.NotionAuthorizeResponse;
import com.fiveoutoffive.moa_backend.integration.application.result.NotionDatabaseResponse;
import com.fiveoutoffive.moa_backend.integration.application.result.NotionExportResponse;
import com.fiveoutoffive.moa_backend.integration.application.result.NotionIntegrationResponse;
import com.fiveoutoffive.moa_backend.integration.application.usecase.NotionIntegrationUseCase;
import com.fiveoutoffive.moa_backend.integration.presentation.api.request.NotionExportRequest;
import com.fiveoutoffive.moa_backend.integration.presentation.api.request.NotionIntegrationUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "16. Integration", description = "외부 연동")
public class NotionIntegrationController {

    private final NotionIntegrationUseCase useCase;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @GetMapping("/rooms/{roomId}/integrations/notion")
    @Operation(summary = "노션 연동 조회")
    public ApiResponse<NotionIntegrationResponse> get(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.get(accountId, roomId));
    }

    @PostMapping("/rooms/{roomId}/integrations/notion/authorize")
    @Operation(summary = "노션 연결 시작")
    public ApiResponse<NotionAuthorizeResponse> authorize(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.authorize(accountId, roomId));
    }

    @GetMapping("/integrations/notion/callback")
    @Operation(summary = "노션 OAuth 콜백")
    public void callback(@RequestParam String code, @RequestParam String state, HttpServletResponse response) throws IOException {
        String target;
        try {
            target = useCase.callback(code, state);
        } catch (BusinessException e) {
            target = frontendUrl + "/?notion=error&code=" + e.getErrorCode().getCode();
        }
        response.sendRedirect(target);
    }

    @GetMapping("/rooms/{roomId}/integrations/notion/databases")
    @Operation(summary = "노션 데이터베이스 목록")
    public ApiResponse<List<NotionDatabaseResponse>> databases(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.getDatabases(accountId, roomId));
    }

    @PutMapping("/rooms/{roomId}/integrations/notion")
    @Operation(summary = "노션 내보내기 설정")
    public ApiResponse<NotionIntegrationResponse> update(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody NotionIntegrationUpdateRequest request) {
        return ApiResponse.success(useCase.update(accountId, roomId, request.toCommand()));
    }

    @PostMapping("/rooms/{roomId}/integrations/notion/exports")
    @Operation(summary = "노션으로 내보내기 (수동)")
    public ApiResponse<NotionExportResponse> export(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody NotionExportRequest request) {
        return ApiResponse.success(useCase.export(accountId, roomId, request.toCommand()));
    }

    @GetMapping("/rooms/{roomId}/integrations/notion/exports")
    @Operation(summary = "노션 내보내기 이력")
    public ApiResponse<PageResponse<NotionExportResponse>> exports(@LoginAccount Long accountId, @PathVariable Long roomId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(useCase.getExports(accountId, roomId, Pages.of(page, size, Sort.by(Sort.Direction.DESC, "exportedAt"))));
    }

    @DeleteMapping("/rooms/{roomId}/integrations/notion")
    @Operation(summary = "노션 연동 해제")
    public ApiResponse<Void> delete(@LoginAccount Long accountId, @PathVariable Long roomId) {
        useCase.delete(accountId, roomId);
        return ApiResponse.empty();
    }
}
