package com.fiveoutoffive.moa_backend.account.presentation.api.request;

import com.fiveoutoffive.moa_backend.account.application.command.AccountUpdateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountUpdateRequest(
        @NotBlank @Size(min=2,max=20) @Pattern(regexp="^[\\p{L}\\p{N} ]+$",message="특수문자는 사용할 수 없습니다.") String name
) {

    public AccountUpdateCommand toCommand() {
        return new AccountUpdateCommand(name);
    }
}
