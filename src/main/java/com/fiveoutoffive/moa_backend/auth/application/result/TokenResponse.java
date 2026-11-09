package com.fiveoutoffive.moa_backend.auth.application.result;

import java.time.LocalDateTime;

public record TokenResponse(String accessToken, LocalDateTime accessTokenExpiresAt) {
}
