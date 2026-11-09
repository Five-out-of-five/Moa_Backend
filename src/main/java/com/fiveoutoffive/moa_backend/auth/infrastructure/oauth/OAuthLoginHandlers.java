package com.fiveoutoffive.moa_backend.auth.infrastructure.oauth;

import com.fiveoutoffive.moa_backend.account.domain.model.SocialProvider;
import com.fiveoutoffive.moa_backend.auth.application.result.OAuthLoginResult;
import com.fiveoutoffive.moa_backend.auth.application.usecase.AuthUseCase;
import com.fiveoutoffive.moa_backend.auth.exception.AuthErrorCode;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.security.GlobalJwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/** 소셜 로그인 콜백 결과를 프론트 주소로 돌려보낸다. 액세스 토큰은 URL 에 싣지 않는다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthLoginHandlers implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

    private final AuthUseCase authUseCase;
    private final GlobalJwtProvider jwtProvider;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String redirect;
        try {
            SocialProvider provider = SocialProvider.valueOf(token.getAuthorizedClientRegistrationId().toUpperCase());
            OAuthUserInfo info = OAuthUserInfo.of(provider, token.getPrincipal().getAttributes());
            OAuthLoginResult result = authUseCase.handleOAuthLogin(provider, info.providerUid(), info.email(), info.name());
            if (result.refreshToken() != null) {
                response.addHeader(HttpHeaders.SET_COOKIE, jwtProvider.createRefreshCookie(result.refreshToken()).toString());
            }
            redirect = frontendUrl + appendReturnPath(result.redirectPath(), request);
        } catch (BusinessException e) {
            redirect = frontendUrl + "/login?error=" + e.getErrorCode().getCode()
                    + (e.getData() != null ? "&until=" + URLEncoder.encode(String.valueOf(e.getData()), StandardCharsets.UTF_8) : "");
        }
        clearReturnPath(response);
        response.sendRedirect(redirect);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        log.warn("[OAuth] 로그인 실패: {}", exception.getMessage());
        clearReturnPath(response);
        response.sendRedirect(frontendUrl + "/login?error=" + AuthErrorCode.AUTH_PROVIDER_NOT_SUPPORTED.getCode());
    }

    private String appendReturnPath(String path, HttpServletRequest request) {
        if (request.getCookies() == null) {
            return path;
        }
        for (Cookie cookie : request.getCookies()) {
            if (ReturnPathAuthorizationRequestResolver.RETURN_PATH_COOKIE.equals(cookie.getName())) {
                return path + (path.contains("?") ? "&" : "?") + "returnPath=" + cookie.getValue();
            }
        }
        return path;
    }

    private void clearReturnPath(HttpServletResponse response) {
        Cookie cookie = new Cookie(ReturnPathAuthorizationRequestResolver.RETURN_PATH_COOKIE, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
