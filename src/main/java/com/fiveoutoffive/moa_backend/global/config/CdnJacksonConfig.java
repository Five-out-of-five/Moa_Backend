package com.fiveoutoffive.moa_backend.global.config;

import com.fiveoutoffive.moa_backend.global.infrastructure.s3.CdnUrlSerializerModifier;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.module.SimpleModule;

/**
 * 웹(HTTP 응답) JsonMapper에만 CDN URL 매핑 모디파이어를 등록한다.
 *
 * <p>Redis 캐시용 JsonMapper에는 등록하지 않으므로 캐시에는 상대경로(key)가 그대로 저장되어
 * CDN 주소 변경에 영향을 받지 않고, HTTP 응답 직렬화 시점에만 절대 URL로 변환된다.
 *
 * <p>Spring Boot 4부터 기본 JSON 라이브러리가 Jackson 3(tools.jackson)이라
 * {@code Jackson2ObjectMapperBuilderCustomizer} 대신 {@link JsonMapperBuilderCustomizer}를 사용한다.
 */
@Configuration
public class CdnJacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer cdnUrlJsonMapperCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("CdnUrlModule");
            module.setSerializerModifier(new CdnUrlSerializerModifier());
            builder.addModule(module);
        };
    }
}
