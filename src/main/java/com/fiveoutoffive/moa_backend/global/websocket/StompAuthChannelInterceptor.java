package com.fiveoutoffive.moa_backend.global.websocket;

import com.fiveoutoffive.moa_backend.global.security.GlobalJwtProvider;
import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * STOMP CONNECT 를 인증한다. (API 명세 '[STOMP] 웹소켓 연결', 11/12 회의 D안)
 *
 * <p>토큰은 두 곳에서 찾는다. 헤더가 있으면 헤더만 쓰고, 헤더가 틀려도 쿠키로 넘어가지 않는다.
 * <ol>
 *   <li>CONNECT 프레임의 {@code Authorization: Bearer} 헤더 — {@code client.connectHeaders = { Authorization: `Bearer ${token}` }}</li>
 *   <li>헤더가 없으면 핸드셰이크에 실린 accessToken 쿠키 ({@link JwtHandshakeInterceptor} 가 세션 속성에 넣어 둔 값).
 *       프론트 BFF 구조에서 토큰이 httpOnly 쿠키에만 있을 때 쓰인다.</li>
 * </ol>
 *
 * <p>검증 결과는 세션 속성(principal, role, userId)과 STOMP 사용자로 저장해 이후 프레임에서 재사용한다.
 * 연결 중에 토큰이 만료돼도 이미 맺은 연결은 유지되며, 재연결할 때 새 토큰을 보내면 된다.
 * 토큰이 없거나 틀리면 예외를 던져 CONNECT 를 거절한다(클라이언트는 ERROR 프레임을 받는다).
 *
 * <p>SUBSCRIBE·SEND 는 {@link StompDestinationAuthorizer} 로 방 멤버·채널 참여 여부를 확인한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    /** 클라이언트가 보낼 수 있는 경로. /topic·/user 로 직접 보내면 브로커가 그대로 퍼뜨려 이벤트·알림을 위조할 수 있다. */
    private static final String SEND_PREFIX = "/app/";
    private static final String TOPIC_PREFIX = "/topic/";
    /** 구독할 수 있는 개인 큐. /user/{다른 사용자}/... 같은 경로는 받지 않는다. */
    private static final Set<String> USER_QUEUES = Set.of("/user/queue/notifications", "/user/queue/channels");

    private final GlobalJwtProvider globalJwtProvider;
    private final ObjectProvider<WebSocketUserPort> webSocketUserPortProvider;
    private final ObjectProvider<StompDestinationAuthorizer> destinationAuthorizerProvider;
    /** PresenceEventListener → SimpMessagingTemplate → STOMP 설정 → 이 클래스의 순환을 끊기 위해 지연 조회한다. */
    private final ObjectProvider<PresenceEventListener> presenceEventListenerProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        if (accessor.getMessageType() == SimpMessageType.HEARTBEAT) {
            presenceEventListenerProvider.getObject().touch(accessor.getUser());
            return message;
        }
        StompCommand command = accessor.getCommand();
        if (command == StompCommand.CONNECT) {
            authenticate(accessor);
        } else if (command == StompCommand.SUBSCRIBE || command == StompCommand.SEND) {
            authorize(accessor, command);
        }
        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String token = resolveToken(accessor);
        if (token == null) {
            throw reject("Authorization 헤더와 accessToken 쿠키 모두 없음");
        }

        Claims claims;
        try {
            // parseClaims는 만료/위조 사유를 예외로 구분해준다. validateToken(boolean)만 쓰면 사유가 사라진다.
            claims = globalJwtProvider.parseClaims(token);
        } catch (Exception e) {
            // 만료·위조·서명 불일치 모두 여기로 온다. 접속을 거절하는 동작은 같으므로 사유만 남긴다.
            throw reject("토큰 검증 실패 (" + e.getMessage() + ")");
        }

        String principal = claims.getSubject();
        if (principal == null || principal.isBlank()) {
            throw reject("토큰에 subject 없음");
        }

        String role = claims.get("role", String.class);
        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes == null) {
            throw reject("세션 속성 없음");
        }
        attributes.put(WebSocketSessionAttributes.PRINCIPAL, principal);
        if (role != null) {
            attributes.put(WebSocketSessionAttributes.ROLE, role);
        }

        WebSocketUserPort userPort = webSocketUserPortProvider.getIfAvailable();
        if (userPort != null) {
            Long userId = userPort.findUserIdByPrincipal(principal).orElse(null);
            if (userId == null) {
                throw reject("사용자를 찾을 수 없음 principal=" + principal);
            }
            attributes.put(WebSocketSessionAttributes.USER_ID, userId);
        }

        var authorities = StringUtils.hasText(role)
                ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                : List.<SimpleGrantedAuthority>of();
        // convertAndSendToUser(/user/queue/**) 가 이 이름으로 세션을 찾는다.
        accessor.setUser(new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }

    private void authorize(StompHeaderAccessor accessor, StompCommand command) {
        String destination = accessor.getDestination();
        if (destination == null || !allowedForCommand(command, destination)) {
            throw new MessageDeliveryException("허용되지 않은 경로입니다: " + command + " " + destination);
        }
        StompDestinationAuthorizer authorizer = destinationAuthorizerProvider.getIfAvailable();
        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (authorizer == null || attributes == null) {
            return;
        }
        Long userId = (Long) attributes.get(WebSocketSessionAttributes.USER_ID);
        if (userId == null || !authorizer.canAccess(userId, destination)) {
            throw new MessageDeliveryException("접근 권한이 없는 경로입니다: " + destination);
        }
    }

    /** SEND 는 /app/** 만, SUBSCRIBE 는 /topic/** 와 본인 개인 큐만 허용한다. 방·채널 권한은 이어서 authorizer 가 본다. */
    private boolean allowedForCommand(StompCommand command, String destination) {
        if (command == StompCommand.SEND) {
            return destination.startsWith(SEND_PREFIX);
        }
        return destination.startsWith(TOPIC_PREFIX) || USER_QUEUES.contains(destination);
    }

    /** 헤더 우선, 없으면 핸드셰이크 쿠키. */
    private String resolveToken(StompHeaderAccessor accessor) {
        String header = resolveBearerToken(accessor);
        if (header != null) {
            return header;
        }
        Map<String, Object> attributes = accessor.getSessionAttributes();
        Object cookie = attributes == null ? null : attributes.get(WebSocketSessionAttributes.HANDSHAKE_ACCESS_TOKEN);
        return cookie instanceof String value && StringUtils.hasText(value) ? value : null;
    }

    private String resolveBearerToken(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(header) || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return header.substring(BEARER_PREFIX.length());
    }

    private MessageDeliveryException reject(String reason) {
        log.warn("[WebSocket] CONNECT 거절: {}", reason);
        return new MessageDeliveryException("STOMP 인증에 실패했습니다.");
    }
}
