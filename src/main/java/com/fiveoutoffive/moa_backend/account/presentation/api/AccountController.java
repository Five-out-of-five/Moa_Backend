package com.fiveoutoffive.moa_backend.account.presentation.api;

import com.fiveoutoffive.moa_backend.account.application.result.MyAccountResponse;
import com.fiveoutoffive.moa_backend.account.application.result.WithdrawalCheckResponse;
import com.fiveoutoffive.moa_backend.account.application.usecase.AccountUseCase;
import com.fiveoutoffive.moa_backend.account.presentation.api.request.AccountUpdateRequest;
import com.fiveoutoffive.moa_backend.account.presentation.api.request.AiSettingsUpdateRequest;
import com.fiveoutoffive.moa_backend.account.presentation.api.request.LastRoomUpdateRequest;
import com.fiveoutoffive.moa_backend.account.presentation.api.request.WithdrawRequest;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.security.GlobalJwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts/me")
@RequiredArgsConstructor
@Tag(name = "02. Account", description = "계정")
public class AccountController {

    private final AccountUseCase accountUseCase;
    private final GlobalJwtProvider jwtProvider;

    @GetMapping
    @Operation(summary = "내 계정 조회")
    public ApiResponse<MyAccountResponse> getMe(@LoginAccount Long accountId) {
        return ApiResponse.success(accountUseCase.getMe(accountId));
    }

    @PatchMapping
    @Operation(summary = "기본 프로필(이름) 수정")
    public ApiResponse<MyAccountResponse> updateMe(@LoginAccount Long accountId, @Valid @RequestBody AccountUpdateRequest request) {
        return ApiResponse.success(accountUseCase.updateMe(accountId, request.toCommand()));
    }

    @PatchMapping("/ai-settings")
    @Operation(summary = "AI 설정 변경 (톤앤매너 강도)")
    public ApiResponse<MyAccountResponse> updateAiSettings(@LoginAccount Long accountId, @Valid @RequestBody AiSettingsUpdateRequest request) {
        return ApiResponse.success(accountUseCase.updateAiSettings(accountId, request.toCommand()));
    }

    @PutMapping("/last-room")
    @Operation(summary = "마지막으로 머문 방 저장")
    public ApiResponse<Void> updateLastRoom(@LoginAccount Long accountId, @Valid @RequestBody LastRoomUpdateRequest request) {
        accountUseCase.updateLastRoom(accountId, request.toCommand());
        return ApiResponse.empty();
    }

    @GetMapping("/withdrawal-check")
    @Operation(summary = "탈퇴 가능 여부 확인")
    public ApiResponse<WithdrawalCheckResponse> checkWithdrawal(@LoginAccount Long accountId) {
        return ApiResponse.success(accountUseCase.checkWithdrawal(accountId));
    }

    @DeleteMapping
    @Operation(summary = "회원 탈퇴")
    public ApiResponse<Void> withdraw(@LoginAccount Long accountId, @Valid @RequestBody WithdrawRequest request, HttpServletResponse response) {
        accountUseCase.withdraw(accountId, request.toCommand());
        response.addHeader(HttpHeaders.SET_COOKIE, jwtProvider.deleteRefreshCookie().toString());
        return ApiResponse.empty();
    }
}
