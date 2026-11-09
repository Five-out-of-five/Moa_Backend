package com.fiveoutoffive.moa_backend.auth.application.result;

public record OAuthLoginResult(String redirectPath, String refreshToken) {
}
