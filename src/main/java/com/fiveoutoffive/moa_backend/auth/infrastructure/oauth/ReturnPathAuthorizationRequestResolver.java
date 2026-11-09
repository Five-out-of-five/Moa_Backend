package com.fiveoutoffive.moa_backend.auth.infrastructure.oauth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/** returnPath 쿼리를 쿠키에 담아 콜백까지 전달한다. 외부 주소는 오픈 리다이렉트 방지를 위해 무시한다. */
@Component
public class ReturnPathAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    public static final String RETURN_PATH_COOKIE = "oauth2_return_path";
    private static final String BASE_URI = "/oauth2/authorization";
    private static final int COOKIE_MAX_AGE_SECONDS = 300;

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    public ReturnPathAuthorizationRequestResolver(ClientRegistrationRepository repository) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(repository, BASE_URI);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest resolved = delegate.resolve(request);
        if (resolved != null) {
            remember(request);
        }
        return resolved;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest resolved = delegate.resolve(request, clientRegistrationId);
        if (resolved != null) {
            remember(request);
        }
        return resolved;
    }

    private void remember(HttpServletRequest request) {
        var attributes = (org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        String returnPath = request.getParameter("returnPath");
        if (attributes == null || attributes.getResponse() == null || !isInternalPath(returnPath)) {
            return;
        }
        Cookie cookie = new Cookie(RETURN_PATH_COOKIE, java.net.URLEncoder.encode(returnPath, StandardCharsets.UTF_8));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
        attributes.getResponse().addCookie(cookie);
    }

    static boolean isInternalPath(String path) {
        return path != null && path.startsWith("/") && !path.startsWith("//") && !path.contains("\\");
    }
}
