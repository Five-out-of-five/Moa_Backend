package com.fiveoutoffive.moa_backend.global.websocket;

import com.fiveoutoffive.moa_backend.global.security.GlobalJwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class StompAuthChannelInterceptorTest {

    private final GlobalJwtProvider jwtProvider = mock(GlobalJwtProvider.class);
    private final WebSocketUserPort userPort = mock(WebSocketUserPort.class);
    private final StompDestinationAuthorizer authorizer = mock(StompDestinationAuthorizer.class);
    private StompAuthChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        ObjectProvider<WebSocketUserPort> userProvider = mock(ObjectProvider.class);
        ObjectProvider<StompDestinationAuthorizer> authorizerProvider = mock(ObjectProvider.class);
        ObjectProvider<PresenceEventListener> presenceProvider = mock(ObjectProvider.class);
        when(userProvider.getIfAvailable()).thenReturn(userPort);
        when(authorizerProvider.getIfAvailable()).thenReturn(authorizer);
        interceptor = new StompAuthChannelInterceptor(jwtProvider, userProvider, authorizerProvider, presenceProvider);
    }

    @Test
    void Bearer_헤더로_CONNECT_하면_세션에_사용자가_저장된다() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("7");
        when(claims.get("role", String.class)).thenReturn("USER");
        when(jwtProvider.parseClaims("good")).thenReturn(claims);
        when(userPort.findUserIdByPrincipal("7")).thenReturn(Optional.of(7L));
        Map<String, Object> attributes = new HashMap<>();

        Message<?> result = interceptor.preSend(frame(StompCommand.CONNECT, "Bearer good", attributes, null), null);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
        assertThat(attributes.get(WebSocketSessionAttributes.USER_ID)).isEqualTo(7L);
        assertThat(attributes.get(WebSocketSessionAttributes.PRINCIPAL)).isEqualTo("7");
        assertThat(attributes.get(WebSocketSessionAttributes.ROLE)).isEqualTo("USER");
        assertThat(accessor.getUser()).isNotNull();
        assertThat(accessor.getUser().getName()).isEqualTo("7");
    }

    @Test
    void 헤더가_없으면_CONNECT_를_거절한다() {
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT, null, new HashMap<>(), null), null))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void 토큰이_틀리면_CONNECT_를_거절한다() {
        when(jwtProvider.parseClaims(anyString())).thenThrow(new JwtException("bad signature"));

        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT, "Bearer bad", new HashMap<>(), null), null))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void 권한_없는_경로_구독은_거절한다() {
        Map<String, Object> attributes = new HashMap<>(Map.of(WebSocketSessionAttributes.USER_ID, 7L));
        when(authorizer.canAccess(7L, "/topic/rooms/1")).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SUBSCRIBE, null, attributes, "/topic/rooms/1"), null))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void 권한_있는_경로_구독은_통과한다() {
        Map<String, Object> attributes = new HashMap<>(Map.of(WebSocketSessionAttributes.USER_ID, 7L));
        when(authorizer.canAccess(7L, "/topic/rooms/1")).thenReturn(true);

        assertThat(interceptor.preSend(frame(StompCommand.SUBSCRIBE, null, attributes, "/topic/rooms/1"), null)).isNotNull();
    }

    private Message<byte[]> frame(StompCommand command, String authorization, Map<String, Object> attributes, String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        if (authorization != null) {
            accessor.setNativeHeader("Authorization", authorization);
        }
        if (destination != null) {
            accessor.setDestination(destination);
        }
        accessor.setSessionAttributes(attributes);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
