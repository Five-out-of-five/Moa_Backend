package com.fiveoutoffive.moa_backend.account.presentation.api.request;

import com.fiveoutoffive.moa_backend.account.application.command.WithdrawCommand;
import jakarta.validation.constraints.NotBlank;

public record WithdrawRequest(
        @NotBlank String confirmText
) {

    public WithdrawCommand toCommand() {
        return new WithdrawCommand(confirmText);
    }
}
