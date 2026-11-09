package com.fiveoutoffive.moa_backend.account.application.usecase;

import com.fiveoutoffive.moa_backend.account.application.command.AccountUpdateCommand;
import com.fiveoutoffive.moa_backend.account.application.command.AiSettingsUpdateCommand;
import com.fiveoutoffive.moa_backend.account.application.command.LastRoomUpdateCommand;
import com.fiveoutoffive.moa_backend.account.application.command.WithdrawCommand;
import com.fiveoutoffive.moa_backend.account.application.result.MyAccountResponse;
import com.fiveoutoffive.moa_backend.account.application.result.WithdrawalCheckResponse;

public interface AccountUseCase {

    MyAccountResponse getMe(Long accountId);

    MyAccountResponse updateMe(Long accountId, AccountUpdateCommand command);

    MyAccountResponse updateAiSettings(Long accountId, AiSettingsUpdateCommand command);

    void updateLastRoom(Long accountId, LastRoomUpdateCommand command);

    WithdrawalCheckResponse checkWithdrawal(Long accountId);

    void withdraw(Long accountId, WithdrawCommand command);
}
