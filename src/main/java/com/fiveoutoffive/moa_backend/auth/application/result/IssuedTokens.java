package com.fiveoutoffive.moa_backend.auth.application.result;

public record IssuedTokens(TokenResponse token, String refreshToken) {
}
