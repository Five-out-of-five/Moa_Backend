package com.fiveoutoffive.moa_backend.auth.infrastructure.oauth;

import com.fiveoutoffive.moa_backend.account.domain.model.SocialProvider;
import java.util.Map;

/** 제공자별 사용자 정보 응답을 공통 형태로 바꾼다. 이름·이메일만 받고 프로필 이미지는 받지 않는다. */
public record OAuthUserInfo(String providerUid, String email, String name) {

    @SuppressWarnings("unchecked")
    public static OAuthUserInfo of(SocialProvider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> new OAuthUserInfo(str(attributes.get("sub")), str(attributes.get("email")), str(attributes.get("name")));
            case KAKAO -> {
                Map<String, Object> account = (Map<String, Object>) attributes.getOrDefault("kakao_account", Map.of());
                Map<String, Object> profile = (Map<String, Object>) account.getOrDefault("profile", Map.of());
                yield new OAuthUserInfo(str(attributes.get("id")), str(account.get("email")), str(profile.get("nickname")));
            }
            case NAVER -> {
                Map<String, Object> response = (Map<String, Object>) attributes.getOrDefault("response", Map.of());
                yield new OAuthUserInfo(str(response.get("id")), str(response.get("email")), str(response.get("name")));
            }
        };
    }

    private static String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
