package com.fiveoutoffive.moa_backend.auth.presentation.api.request;

import com.fiveoutoffive.moa_backend.auth.application.command.SignupCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank String ticket,
        @NotBlank @Size(min=2,max=20) @Pattern(regexp="^[\\p{L}\\p{N} ]+$",message="특수문자는 사용할 수 없습니다.") String name,
        @NotNull Boolean termsAgreed,
        @NotNull Boolean privacyAgreed
) {

    public SignupCommand toCommand() {
        return new SignupCommand(ticket, name, termsAgreed, privacyAgreed);
    }
}
