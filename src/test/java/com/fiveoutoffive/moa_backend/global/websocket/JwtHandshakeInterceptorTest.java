package com.fiveoutoffive.moa_backend.global.websocket;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class JwtHandshakeInterceptorTest {

    private final JwtHandshakeInterceptor interceptor = new JwtHandshakeInterceptor();

    @Test
    void accessToken_쿠키를_세션_속성에_옮긴다() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ws");
        request.setCookies(new Cookie("other", "x"), new Cookie("accessToken", "token-value"));
        Map<String, Object> attributes = new HashMap<>();

        boolean proceed = handshake(request, attributes);

        assertThat(proceed).isTrue();
        assertThat(attributes).containsEntry(WebSocketSessionAttributes.HANDSHAKE_ACCESS_TOKEN, "token-value");
    }

    @Test
    void 쿠키가_없어도_핸드셰이크는_통과하고_속성은_비운다() {
        Map<String, Object> attributes = new HashMap<>();

        boolean proceed = handshake(new MockHttpServletRequest("GET", "/ws"), attributes);

        assertThat(proceed).isTrue();
        assertThat(attributes).doesNotContainKey(WebSocketSessionAttributes.HANDSHAKE_ACCESS_TOKEN);
    }

    @Test
    void 빈_accessToken_쿠키는_무시한다() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ws");
        request.setCookies(new Cookie("accessToken", ""));
        Map<String, Object> attributes = new HashMap<>();

        handshake(request, attributes);

        assertThat(attributes).doesNotContainKey(WebSocketSessionAttributes.HANDSHAKE_ACCESS_TOKEN);
    }

    private boolean handshake(MockHttpServletRequest request, Map<String, Object> attributes) {
        return interceptor.beforeHandshake(new ServletServerHttpRequest(request),
                new ServletServerHttpResponse(new MockHttpServletResponse()), null, attributes);
    }
}
