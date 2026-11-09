package com.fiveoutoffive.moa_backend.global.websocket;

import com.fiveoutoffive.moa_backend.global.util.RedisKeys;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 현재 접속 중인 계정 조회. Redis 장애 시에는 모두 오프라인으로 본다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceReader {

    private final StringRedisTemplate redisTemplate;

    public Set<Long> onlineOf(Collection<Long> accountIds) {
        List<Long> ids = List.copyOf(accountIds);
        if (ids.isEmpty()) {
            return Set.of();
        }
        try {
            List<String> values = redisTemplate.opsForValue().multiGet(ids.stream().map(id -> RedisKeys.ONLINE_PREFIX + id).toList());
            Set<Long> online = new HashSet<>();
            for (int i = 0; values != null && i < ids.size(); i++) {
                if (values.get(i) != null) {
                    online.add(ids.get(i));
                }
            }
            return online;
        } catch (Exception e) {
            log.warn("[Presence] Redis 조회 실패: {}", e.getMessage());
            return Set.of();
        }
    }
}
