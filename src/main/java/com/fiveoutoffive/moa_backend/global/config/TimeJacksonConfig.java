package com.fiveoutoffive.moa_backend.global.config;

import com.fiveoutoffive.moa_backend.global.util.KstTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * 응답의 시각을 한국 시간으로 내보낸다. (API 명세 공통 규칙 "날짜·시간")
 *
 * <p>애플리케이션의 LocalDateTime 은 모두 UTC 값이다. 응답 DTO 마다 변환하면 빠뜨리기 쉬우므로
 * 직렬화 시점 한 곳에서 +09:00 ISO-8601 문자열로 바꾼다. 웹 JsonMapper 에만 등록되며 Redis 캐시에는 영향이 없다.
 */
@Configuration
public class TimeJacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer kstTimeJsonMapperCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("KstTimeModule");
            module.addSerializer(LocalDateTime.class, new KstLocalDateTimeSerializer());
            builder.addModule(module);
        };
    }

    private static final class KstLocalDateTimeSerializer extends ValueSerializer<LocalDateTime> {

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext ctxt) {
            gen.writeString(KstTime.toKst(value).truncatedTo(ChronoUnit.SECONDS)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
    }
}
