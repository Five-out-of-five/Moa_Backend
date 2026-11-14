package com.fiveoutoffive.moa_backend.global.config;

import com.fiveoutoffive.moa_backend.global.websocket.JwtHandshakeInterceptor;
import com.fiveoutoffive.moa_backend.global.websocket.StompAuthChannelInterceptor;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over WebSocket 설정.
 *
 * <p>경로 규칙
 * <ul>
 *   <li>{@code /ws} — 핸드셰이크 엔드포인트. 인증은 CONNECT 프레임에서 {@link StompAuthChannelInterceptor} 가 담당한다.</li>
 *   <li>{@code /topic/**} — 서버에서 클라이언트로 push하는 구독 경로</li>
 *   <li>{@code /app/**} — 클라이언트가 서버로 보낼 때 붙이는 prefix. {@code @MessageMapping} 이 받는다.</li>
 *   <li>{@code /user/**} — 특정 사용자에게만 보낼 때 사용한다. ({@code convertAndSendToUser})</li>
 * </ul>
 *
 * <p>브로커는 인메모리 SimpleBroker다. 서버를 여러 대로 늘리면 인스턴스 간 push가 전달되지 않으므로,
 * 그 시점에 외부 브로커(Redis Pub/Sub 릴레이, RabbitMQ STOMP 등)로 교체해야 한다.
 *
 * <p>허용 오리진은 REST와 같은 값({@code app.cors.allowed-origins})을 쓴다. WebSocket 핸드셰이크는
 * 시큐리티의 CORS 설정을 타지 않으므로 여기서 따로 지정해야 한다.
 *
 * <p>메시지 핸들러 추가 예시
 * <pre>{@code
 * @Controller
 * @RequiredArgsConstructor
 * public class ChatMessageHandler {
 *
 *     private final SimpMessagingTemplate messagingTemplate;
 *
 *     // 클라이언트: stompClient.send("/app/chat/rooms/1", {}, body)
 *     @MessageMapping("/chat/rooms/{roomId}")
 *     public void send(@DestinationVariable Long roomId,
 *                      @Payload ChatMessageSendRequest request,
 *                      SimpMessageHeaderAccessor accessor) {
 *         Long userId = (Long) accessor.getSessionAttributes().get(WebSocketSessionAttributes.USER_ID);
 *         messagingTemplate.convertAndSend("/topic/chat/rooms/" + roomId, ...);
 *     }
 * }
 * }</pre>
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /** 클라이언트가 20초마다 신호를 보낸다. 3회(60초) 끊기면 SimpleBroker 가 세션을 닫아 오프라인 처리된다. */
    private static final long HEARTBEAT_MILLIS = 20_000;

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;
    private final TaskScheduler webSocketHeartbeatScheduler;

    /** 핸드셰이크 경로. 바꾸면 GlobalSecurityConfig의 permitAll 경로도 함께 맞춰야 한다. */
    @Value("${app.websocket.endpoint}")
    private String endpoint;

    /** REST와 동일한 허용 오리진 목록을 사용한다. */
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{HEARTBEAT_MILLIS, HEARTBEAT_MILLIS})
                .setTaskScheduler(webSocketHeartbeatScheduler);
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // SockJS 폴백은 켜지 않는다. 필요하면 .withSockJS() 를 붙이되, 프론트엔드도 SockJS 클라이언트를 써야 한다.
        registry.addEndpoint(endpoint)
                .setAllowedOriginPatterns(parseOrigins())
                .addInterceptors(jwtHandshakeInterceptor);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }

    private String[] parseOrigins() {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        // /ws 는 accessToken 쿠키로도 인증하므로 Origin 허용 목록이 유일한 CSRF 방어선이다. 와일드카드는 막는다.
        if (origins.isEmpty() || origins.stream().anyMatch(origin -> origin.equals("*") || origin.contains("://*"))) {
            throw new IllegalStateException("app.cors.allowed-origins(CORS_ALLOWED_ORIGINS) 에 와일드카드를 쓸 수 없습니다. "
                    + "WebSocket 쿠키 인증 때문에 프론트 주소를 정확히 적어야 합니다. 현재 값: " + allowedOrigins);
        }

        return origins.toArray(String[]::new);
    }
}
