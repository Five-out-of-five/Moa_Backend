package com.fiveoutoffive.moa_backend.auth.infrastructure.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fiveoutoffive.moa_backend.auth.application.usecase.AuthUseCase;
import com.fiveoutoffive.moa_backend.auth.exception.AuthErrorCode;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.security.GlobalJwtProvider;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("소셜 로그인 콜백 리다이렉트")
class OAuthLoginHandlersTest {

    private final AuthUseCase authUseCase = mock(AuthUseCase.class);
    private final OAuthLoginHandlers handlers = new OAuthLoginHandlers(authUseCase, mock(GlobalJwtProvider.class));

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handlers, "frontendUrl", "https://moayo.site");
    }

    private String redirectWhenLoginThrows(BusinessException e) throws Exception {
        when(authUseCase.handleOAuthLogin(any(), anyString(), anyString(), anyString())).thenThrow(e);
        DefaultOAuth2User user = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", "uid-1", "email", "a@b.com", "name", "회원"), "sub");
        MockHttpServletResponse response = new MockHttpServletResponse();
        handlers.onAuthenticationSuccess(new MockHttpServletRequest(), response,
                new OAuth2AuthenticationToken(user, user.getAuthorities(), "google"));
        return URLDecoder.decode(response.getRedirectedUrl(), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("기간 정지면 until 을 응답 본문과 같은 한국 시각(+09:00)으로 붙인다")
    void suspendedUntilIsKst() throws Exception {
        LocalDateTime utc = LocalDateTime.of(2026, 11, 25, 3, 0, 0, 123_456_000);

        String url = redirectWhenLoginThrows(new BusinessException(AuthErrorCode.AUTH_ACCOUNT_SUSPENDED, utc));

        assertThat(url).isEqualTo("https://moayo.site/login?error=AUTH_ACCOUNT_SUSPENDED&until=2026-11-25T12:00:00+09:00");
    }

    @Test
    @DisplayName("영구 정지는 until 을 붙이지 않는다")
    void permanentHasNoUntil() throws Exception {
        String url = redirectWhenLoginThrows(new BusinessException(AuthErrorCode.AUTH_ACCOUNT_SUSPENDED, null));

        assertThat(url).isEqualTo("https://moayo.site/login?error=AUTH_ACCOUNT_SUSPENDED");
    }
}
