package com.fiveoutoffive.moa_backend.admin.application.usecase;

import com.fiveoutoffive.moa_backend.account.domain.model.AccountRole;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountStatus;
import com.fiveoutoffive.moa_backend.account.domain.model.SocialProvider;
import com.fiveoutoffive.moa_backend.admin.application.command.RoleChangeCommand;
import com.fiveoutoffive.moa_backend.admin.application.command.SuspendCommand;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminAccountDetailResponse;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminAccountResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;

public interface AdminAccountUseCase {

    PageResponse<AdminAccountResponse> getAccounts(String keyword, SocialProvider provider, AccountStatus status, AccountRole role, LocalDate joinedFrom, LocalDate joinedTo, Pageable pageable);

    AdminAccountDetailResponse getAccount(Long accountId);

    AdminAccountDetailResponse suspend(Long adminId, Long accountId, SuspendCommand command);

    AdminAccountDetailResponse release(Long adminId, Long accountId);

    AdminAccountDetailResponse changeRole(Long adminId, Long accountId, RoleChangeCommand command);
}
