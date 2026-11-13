package com.fiveoutoffive.moa_backend.global.infrastructure.ai;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Python AI 서버와의 연결을 관리한다.
 *
 * <p>JDK HttpClient 의 커넥션 풀로 keep-alive 연결을 재사용하고, 주기적인 헬스 체크로 연결을 유지·복구한다.
 * 서버가 없거나 끊겨도 애플리케이션은 정상 동작한다: 기동을 막지 않고, 끊긴 동안의 호출은 네트워크를 타지 않고
 * 바로 "사용 불가"로 처리하며, 상태가 바뀔 때만 로그를 남긴다.
 */
@Slf4j
@Component
public class AiServerConnection {

    private static final String HEALTH_PATH = "/health";
    /** Python 서버가 내부 호출인지 확인하는 헤더. 값이 비어 있으면 보내지 않는다(로컬). */
    private static final String INTERNAL_KEY_HEADER = "X-Internal-Api-Key";

    private final RestClient restClient;
    private final String serverUrl;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public AiServerConnection(
            @Value("${app.ai.server-url}") String serverUrl,
            @Value("${app.ai.connect-timeout}") Duration connectTimeout,
            @Value("${app.ai.read-timeout}") Duration readTimeout,
            @Value("${app.ai.internal-key:}") String internalKey
    ) {
        this.serverUrl = serverUrl;
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);
        RestClient.Builder builder = RestClient.builder().baseUrl(serverUrl).requestFactory(requestFactory);
        if (internalKey != null && !internalKey.isBlank()) {
            builder.defaultHeader(INTERNAL_KEY_HEADER, internalKey);
        }
        this.restClient = builder.build();
    }

    public RestClient client() {
        return restClient;
    }

    public boolean isConnected() {
        return connected.get();
    }

    /** 호출이 실패했을 때 어댑터가 알린다. 다음 헬스 체크가 성공할 때까지 호출을 건너뛴다. */
    public void markDisconnected(String reason) {
        if (connected.compareAndSet(true, false)) {
            log.warn("[AI] Python 서버 연결 끊김: {}", reason);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void connectOnStartup() {
        checkHealth();
    }

    @Scheduled(fixedDelayString = "${app.ai.health-interval}")
    public void checkHealth() {
        try {
            restClient.get().uri(HEALTH_PATH).retrieve().toBodilessEntity();
            if (connected.compareAndSet(false, true)) {
                log.info("[AI] Python 서버 연결됨: {}", serverUrl);
            }
        } catch (Exception e) {
            markDisconnected(e.getMessage());
        }
    }
}
