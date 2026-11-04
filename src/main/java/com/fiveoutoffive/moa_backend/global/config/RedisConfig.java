package com.fiveoutoffive.moa_backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

/**
 * Redis 수동 사용(RedisTemplate)을 위한 설정.
 *
 * <p>키/값 직렬화 방식을 명시해두면 redis-cli로 값을 확인할 때 사람이 읽을 수 있는 형태로 저장된다.
 * (기본 JdkSerializationRedisSerializer는 바이너리로 저장되어 디버깅이 어렵다)
 */
@Configuration
public class RedisConfig {

    /** 역직렬화를 허용할 애플리케이션 패키지. 여기 없는 타입은 @class에 뭐가 적혀 있어도 복원하지 않는다. */
    private static final String APPLICATION_BASE_PACKAGE = "com.fiveoutoffive.moa_backend.";

    /** 문자열 전용 템플릿. 리프레시 토큰, 인증코드, 플래그 등 단순 값 저장에 사용한다. */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    /**
     * 객체를 JSON으로 저장하는 템플릿. DTO 캐싱 등에 사용한다.
     *
     * <p>값에 {@code @class}를 함께 저장해야 꺼낼 때 원래 타입으로 복원된다.
     * 타입 정보가 없으면 {@code LinkedHashMap}으로 돌아와 캐스팅 지점에서 터진다.
     */
    @Bean
    public RedisTemplate<String, Object> objectRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // enableDefaultTyping: 값에 @class 타입 정보를 붙여 저장한다. 허용 패키지는 typeValidator로 좁힌다.
        // Spring Boot 4부터 Jackson 3 기반 GenericJacksonJsonRedisSerializer를 사용한다.
        GenericJacksonJsonRedisSerializer valueSerializer = GenericJacksonJsonRedisSerializer
                .builder(RedisConfig::redisJsonMapperBuilder)
                .enableDefaultTyping(typeValidator())
                .build();

        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        return template;
    }

    /**
     * Redis 전용 JsonMapper.
     *
     * <p>웹 JsonMapper를 재사용하지 않는 이유가 두 가지다.
     * <ol>
     *   <li>웹 쪽에는 {@code CdnUrlSerializerModifier}가 붙어 있어, 그대로 쓰면 캐시에
     *       object key가 아니라 CDN 절대 URL이 저장된다. CDN 주소를 바꾸면 캐시가 전부 무효가 된다.</li>
     *   <li>캐시는 타입 복원을 위해 default typing이 필요한데, 이걸 웹 응답에 켜면
     *       모든 API 응답에 {@code @class} 필드가 노출된다.</li>
     * </ol>
     */
    private static JsonMapper.Builder redisJsonMapperBuilder() {
        // Jackson 3은 java.time(LocalDateTime 등)과 생성자 파라미터 이름 인식을 기본 지원한다.
        // (Jackson 2 때 직접 추가하던 JavaTimeModule, ParameterNamesModule이 필요 없다)
        return JsonMapper.builder()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601로 저장
    }

    private static PolymorphicTypeValidator typeValidator() {
        // default typing은 임의 클래스의 역직렬화 통로가 되므로 허용 대상을 좁힌다.
        return BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(APPLICATION_BASE_PACKAGE)
                .allowIfSubType("java.util.")
                .allowIfSubType("java.time.")
                .build();
    }
}
