package com.fiveoutoffive.moa_backend.ai.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 어시스턴트 스트림 전용 실행기.
 *
 * <p>스트림 하나가 스레드를 최대 120초 붙잡는다. 공용 taskExecutor(@Async)를 같이 쓰면 스트림 몇 개로
 * 충돌 감지·알림·연동 같은 비동기 작업이 밀리므로 따로 둔다. 큐를 두지 않아 꽉 차면 바로 거절되고,
 * AiService 가 질문을 503 AI_UNAVAILABLE 로 돌려준다. 큐에 쌓으면 사용자는 열린 스트림에서 아무것도 못 받는다.
 *
 * <p>@Async 는 빈 이름 taskExecutor 를 쓰므로 이 빈을 더해도 영향이 없다.
 */
@Configuration
public class AssistantStreamExecutorConfig {

    @Bean(name = "assistantStreamExecutor")
    public ThreadPoolTaskExecutor assistantStreamExecutor(@Value("${app.ai.assistant-max-streams:10}") int maxStreams) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(maxStreams);
        executor.setMaxPoolSize(maxStreams);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("ai-assistant-");
        executor.initialize();
        return executor;
    }
}
