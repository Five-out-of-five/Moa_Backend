package com.fiveoutoffive.moa_backend.auth.application.command;

public record SignupCommand(
        String ticket,
        String name,
        Boolean termsAgreed,
        Boolean privacyAgreed
) {
}
