package com.fiveoutoffive.moa_backend.global.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/**
 * 프론트 BFF 구조(11/12 회의 D안)의 WebSocket 쿠키 인증을 실제 서버 포트로 확인한다.
 * 브라우저처럼 핸드셰이크에 accessToken 쿠키와 Origin 헤더를 싣고, CONNECT 프레임에는 Authorization 을 넣지 않는다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StompCookieAuthApiTest extends Be2ApiTestSupport {

    /** application.yaml 의 기본 허용 오리진 (app.cors.allowed-origins) */
    private static final String ALLOWED_ORIGIN = "http://localhost:3000";

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUpClient() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());
        stompClient.setDefaultHeartbeat(new long[]{0, 0});
    }

    @AfterEach
    void stopClient() {
        stompClient.stop();
    }

    @Test
    void 핸드셰이크_쿠키만으로_연결된다() throws Exception {
        Member member = newMember("쿠키회원");

        StompSession session = connect(cookieHeaders(member.token(), ALLOWED_ORIGIN), new StompHeaders());

        assertThat(session.isConnected()).isTrue();
        session.disconnect();
    }

    @Test
    void 헤더도_쿠키도_없으면_연결이_거절된다() {
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.setOrigin(ALLOWED_ORIGIN);

        assertThatThrownBy(() -> connect(headers, new StompHeaders())).isInstanceOf(ExecutionException.class);
    }

    @Test
    void 쿠키_토큰이_틀리면_연결이_거절된다() {
        assertThatThrownBy(() -> connect(cookieHeaders("not-a-jwt", ALLOWED_ORIGIN), new StompHeaders()))
                .isInstanceOf(ExecutionException.class);
    }

    @Test
    void 허용되지_않은_Origin_은_쿠키가_맞아도_핸드셰이크에서_막힌다() {
        Member member = newMember("다른사이트");

        assertThatThrownBy(() -> connect(cookieHeaders(member.token(), "https://evil.kro.kr"), new StompHeaders()))
                .isInstanceOf(ExecutionException.class);
    }

    @Test
    void 기존_Bearer_헤더_방식도_그대로_연결된다() throws Exception {
        Member member = newMember("헤더회원");
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.setOrigin(ALLOWED_ORIGIN);
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + member.token());

        StompSession session = connect(headers, connectHeaders);

        assertThat(session.isConnected()).isTrue();
        session.disconnect();
    }

    @Test
    void 입력_중_표시는_방별_닉네임으로_전달된다() throws Exception {
        Member owner = newMember("계정이름");
        Long roomId = newRoom(owner);
        jdbc.update("UPDATE room_member SET nickname = ? WHERE room_id = ? AND account_id = ?", "방닉네임", roomId, owner.id());
        Long channelId = get(owner, "/rooms/" + roomId + "/channels").ok().first("$.data[?(@.isDefault == true || @.default == true)].channelId");

        StompSession session = connect(cookieHeaders(owner.token(), ALLOWED_ORIGIN), new StompHeaders());
        CompletableFuture<Map<String, Object>> received = new CompletableFuture<>();
        session.subscribe("/topic/channels/" + channelId + "/typing", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                received.complete((Map<String, Object>) payload);
            }
        });
        // SUBSCRIBE 처리가 끝난 뒤 보내도록 잠깐 기다린다. (STOMP 는 구독 완료 응답이 없다)
        TimeUnit.MILLISECONDS.sleep(300);
        session.send("/app/channels/" + channelId + "/typing", Map.of("typing", true));

        Map<String, Object> event = received.get(5, TimeUnit.SECONDS);
        assertThat(event.get("displayName")).isEqualTo("방닉네임");
        assertThat(((Number) event.get("accountId")).longValue()).isEqualTo(owner.id());
        assertThat(event.get("typing")).isEqualTo(true);
        session.disconnect();
    }

    @Test
    void 방_토픽으로_직접_보낸_가짜_이벤트는_전달되지_않는다() throws Exception {
        Member attacker = newMember("위조자");
        Member victim = newMember("피해자");
        Long roomId = newRoom(attacker, victim);
        StompSession victimSession = connect(cookieHeaders(victim.token(), ALLOWED_ORIGIN), new StompHeaders());
        CompletableFuture<Object> received = new CompletableFuture<>();
        victimSession.subscribe("/topic/rooms/" + roomId, collect(received));
        StompSession attackerSession = connect(cookieHeaders(attacker.token(), ALLOWED_ORIGIN), new StompHeaders());
        TimeUnit.MILLISECONDS.sleep(300);

        attackerSession.send("/topic/rooms/" + roomId, Map.of("type", "ROOM_DELETED"));

        assertThatThrownBy(() -> received.get(2, TimeUnit.SECONDS)).isInstanceOf(java.util.concurrent.TimeoutException.class);
        victimSession.disconnect();
    }

    @Test
    void 다른_사람의_개인_큐로_보낸_가짜_알림은_전달되지_않는다() throws Exception {
        Member attacker = newMember("위조자");
        Member victim = newMember("피해자");
        StompSession victimSession = connect(cookieHeaders(victim.token(), ALLOWED_ORIGIN), new StompHeaders());
        CompletableFuture<Object> received = new CompletableFuture<>();
        victimSession.subscribe("/user/queue/notifications", collect(received));
        StompSession attackerSession = connect(cookieHeaders(attacker.token(), ALLOWED_ORIGIN), new StompHeaders());
        TimeUnit.MILLISECONDS.sleep(300);

        attackerSession.send("/user/" + victim.id() + "/queue/notifications", Map.of("notification", Map.of("title", "가짜")));

        assertThatThrownBy(() -> received.get(2, TimeUnit.SECONDS)).isInstanceOf(java.util.concurrent.TimeoutException.class);
        victimSession.disconnect();
    }

    private StompFrameHandler collect(CompletableFuture<Object> received) {
        return new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                received.complete(payload);
            }
        };
    }

    private StompSession connect(WebSocketHttpHeaders headers, StompHeaders connectHeaders) throws Exception {
        return stompClient.connectAsync("ws://localhost:" + port + "/ws", headers, connectHeaders, new StompSessionHandlerAdapter() {
        }).get(5, TimeUnit.SECONDS);
    }

    private WebSocketHttpHeaders cookieHeaders(String token, String origin) {
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.setOrigin(origin);
        headers.put("Cookie", List.of("accessToken=" + token));
        return headers;
    }
}
