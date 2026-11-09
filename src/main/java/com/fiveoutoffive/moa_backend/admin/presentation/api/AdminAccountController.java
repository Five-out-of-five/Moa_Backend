package com.fiveoutoffive.moa_backend.admin.presentation.api;

import com.fiveoutoffive.moa_backend.account.domain.model.AccountRole;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountStatus;
import com.fiveoutoffive.moa_backend.account.domain.model.SocialProvider;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminAccountDetailResponse;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminAccountResponse;
import com.fiveoutoffive.moa_backend.admin.application.usecase.AdminAccountUseCase;
import com.fiveoutoffive.moa_backend.admin.presentation.api.request.RoleChangeRequest;
import com.fiveoutoffive.moa_backend.admin.presentation.api.request.SuspendRequest;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
@Tag(name = "19. Admin", description = "서비스 관리자")
public class AdminAccountController {

    private final AdminAccountUseCase useCase;

    @GetMapping
    @Operation(summary = "[관리자] 회원 목록")
    public ApiResponse<PageResponse<AdminAccountResponse>> list(@RequestParam(required = false) String keyword, @RequestParam(required = false) SocialProvider provider, @RequestParam(required = false) AccountStatus status, @RequestParam(required = false) AccountRole role,
                                                                @RequestParam(required = false) LocalDate joinedFrom, @RequestParam(required = false) LocalDate joinedTo, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(useCase.getAccounts(keyword, provider, status, role, joinedFrom, joinedTo, Pages.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "[관리자] 회원 상세")
    public ApiResponse<AdminAccountDetailResponse> detail(@PathVariable Long accountId) {
        return ApiResponse.success(useCase.getAccount(accountId));
    }

    @PostMapping("/{accountId}/suspension")
    @Operation(summary = "[관리자] 회원 정지")
    public ApiResponse<AdminAccountDetailResponse> suspend(@LoginAccount Long adminId, @PathVariable Long accountId, @Valid @RequestBody SuspendRequest request) {
        return ApiResponse.success(useCase.suspend(adminId, accountId, request.toCommand()));
    }

    @DeleteMapping("/{accountId}/suspension")
    @Operation(summary = "[관리자] 회원 정지 해제")
    public ApiResponse<AdminAccountDetailResponse> release(@LoginAccount Long adminId, @PathVariable Long accountId) {
        return ApiResponse.success(useCase.release(adminId, accountId));
    }

    @PutMapping("/{accountId}/role")
    @Operation(summary = "[관리자] 서비스 권한 변경")
    public ApiResponse<AdminAccountDetailResponse> role(@LoginAccount Long adminId, @PathVariable Long accountId, @Valid @RequestBody RoleChangeRequest request) {
        return ApiResponse.success(useCase.changeRole(adminId, accountId, request.toCommand()));
    }
}
