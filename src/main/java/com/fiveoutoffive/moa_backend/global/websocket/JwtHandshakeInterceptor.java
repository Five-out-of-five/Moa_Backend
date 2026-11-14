package com.fiveoutoffive.moa_backend.global.websocket;

import com.fiveoutoffive.moa_backend.global.security.GlobalJwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * 핸드셰이크 요청에 실린 accessToken 쿠키를 세션 속성에 옮겨 둔다. 핸드셰이크 자체는 거절하지 않는다.
 *
 * <p>프론트 BFF 구조(11/12 회의 D안)에서는 토큰이 httpOnly 쿠키에만 있어 브라우저 JS 가 CONNECT 헤더에 넣을 수 없다.
 * 같은 도메인의 /ws 핸드셰이크에는 쿠키가 자동으로 실리므로, 그 값을 CONNECT 인증에 쓴다.
 * 토큰 검증과 거절은 {@link StompAuthChannelInterceptor} 가 CONNECT 에서 한다. (거절 사유가 STOMP ERROR 프레임으로 가도록)
 *
 * <p>쿠키는 다른 사이트에서 연결해도 실리므로, 허용 오리진 검사(StompWebSocketConfig 의 setAllowedOriginPatterns)가
 * 이 경로의 유일한 방어선이다. 허용 목록 밖의 Origin 은 이 인터셉터보다 먼저 403 으로 막힌다.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = readAccessTokenCookie(request);
        if (token != null) {
            attributes.put(WebSocketSessionAttributes.HANDSHAKE_ACCESS_TOKEN, token);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 후처리할 것이 없다.
    }

    private String readAccessTokenCookie(ServerHttpRequest request) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return null;
        }
        HttpServletRequest httpRequest = servletRequest.getServletRequest();
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (GlobalJwtProvider.ACCESS_TOKEN_COOKIE.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
