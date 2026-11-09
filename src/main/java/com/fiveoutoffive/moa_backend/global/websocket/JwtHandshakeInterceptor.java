package com.fiveoutoffive.moa_backend.global.websocket;

import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * 핸드셰이크는 그대로 통과시킨다.
 *
 * <p>브라우저 WebSocket API 는 핸드셰이크에 Authorization 헤더를 붙일 수 없으므로,
 * 실제 인증은 STOMP CONNECT 프레임에서 {@link StompAuthChannelInterceptor} 가 수행한다.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 후처리할 것이 없다.
    }
}
