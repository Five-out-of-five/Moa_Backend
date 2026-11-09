package com.fiveoutoffive.moa_backend.global.websocket;

import com.fiveoutoffive.moa_backend.global.util.RedisKeys;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * STOMP 접속/해제 시점에 온라인 상태를 Redis에 기록하고, 참여 중인 방에 실시간으로 push한다.
 *
 * <p>접속 상태는 계정 단위다. 여러 탭을 열어도 한 명으로 집계하도록 사용자별 세션 수를 세어
 * 첫 세션에서만 온라인, 마지막 세션이 끊길 때만 오프라인을 보낸다.
 *
 * <p>브라우저 강제 종료는 하트비트가 끊기면서 SimpleBroker 가 60초 후 세션을 닫아 처리된다.
 * Redis 키는 서버가 죽어 disconnect 를 받지 못해도 사라지도록 하트비트마다 TTL 을 갱신한다.
 * Redis 장애가 실시간 연결을 끊지 않도록 Redis 예외는 로그만 남긴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceEventListener {

    private static final long ONLINE_TTL_SECONDS = 90;
    private static final String PRESENCE_DESTINATION = "/topic/rooms/%d/presence";

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final MeterRegistry meterRegistry;
    private final ObjectProvider<PresenceSubscriberPort> presenceSubscriberPortProvider;

    private final Map<Long, AtomicInteger> sessionCounts = new ConcurrentHashMap<>();
    private final AtomicInteger onlineUserCount = new AtomicInteger();

    private Counter connectTotal;
    private Counter disconnectTotal;

    @PostConstruct
    void registerMeters() {
        connectTotal = Counter.builder("websocket.connect.total")
                .description("STOMP 접속 성공 횟수")
                .register(meterRegistry);
        disconnectTotal = Counter.builder("websocket.disconnect.total")
                .description("STOMP 연결 해제 횟수")
                .register(meterRegistry);
        Gauge.builder("websocket.online.users", onlineUserCount, AtomicInteger::get)
                .description("현재 접속 중인 사용자 수 (이 인스턴스 기준)")
                .register(meterRegistry);
    }

    /** CONNECT 처리가 끝난 뒤의 이벤트를 받는다. 시작 시점에 세면 실패한 CONNECT 도 온라인으로 집계된다. */
    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        Long userId = toUserId(event.getUser());
        if (userId == null) {
            return;
        }
        connectTotal.increment();
        writeOnline(userId);
        if (sessionCounts.computeIfAbsent(userId, id -> new AtomicInteger()).incrementAndGet() == 1) {
            onlineUserCount.incrementAndGet();
            log.info("[Presence] 온라인 처리: userId={}", userId);
            broadcast(userId, true);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        Long userId = toUserId(event.getUser());
        if (userId == null) {
            return;
        }
        disconnectTotal.increment();
        AtomicInteger count = sessionCounts.get(userId);
        if (count == null || count.decrementAndGet() > 0) {
            return;
        }
        sessionCounts.remove(userId);
        onlineUserCount.updateAndGet(current -> Math.max(0, current - 1));
        try {
            redisTemplate.delete(RedisKeys.ONLINE_PREFIX + userId);
        } catch (Exception e) {
            log.warn("[Presence] Redis 오프라인 기록 실패: {}", e.getMessage());
        }
        log.info("[Presence] 오프라인 처리: userId={}", userId);
        broadcast(userId, false);
    }

    /** 하트비트를 받을 때마다 온라인 키의 만료를 늘린다. */
    public void touch(Principal user) {
        Long userId = toUserId(user);
        if (userId != null) {
            writeOnline(userId);
        }
    }

    private void writeOnline(Long userId) {
        try {
            redisTemplate.opsForValue().set(RedisKeys.ONLINE_PREFIX + userId, "true", ONLINE_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("[Presence] Redis 온라인 기록 실패: {}", e.getMessage());
        }
    }

    private void broadcast(Long userId, boolean online) {
        PresenceSubscriberPort port = presenceSubscriberPortProvider.getIfAvailable();
        if (port == null) {
            return;
        }
        PresenceEvent payload = new PresenceEvent(userId, online);
        port.findRoomIds(userId).forEach(roomId ->
                messagingTemplate.convertAndSend(PRESENCE_DESTINATION.formatted(roomId), payload));
    }

    /** JWT subject 는 계정 ID 다. CONNECT 인증에 실패한 세션은 user 가 없다. */
    private Long toUserId(Principal user) {
        if (user == null) {
            return null;
        }
        try {
            return Long.valueOf(user.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
