package com.fiveoutoffive.moa_backend.global.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fiveoutoffive.moa_backend.global.config.StompWebSocketConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** /ws 는 쿠키로도 인증하므로 허용 오리진에 와일드카드를 쓰면 기동을 막는다. */
class StompOriginConfigTest {

    @Test
    void 정확한_오리진_목록은_그대로_쓴다() {
        assertThat(parse("https://moarang.kro.kr, http://localhost:3000"))
                .containsExactly("https://moarang.kro.kr", "http://localhost:3000");
    }

    @Test
    void 와일드카드_오리진은_거절한다() {
        assertThatThrownBy(() -> parse("*")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> parse("https://*.kro.kr")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 비어_있으면_거절한다() {
        assertThatThrownBy(() -> parse(" , ")).isInstanceOf(IllegalStateException.class);
    }

    private String[] parse(String origins) {
        StompWebSocketConfig config = new StompWebSocketConfig(null, null, null);
        ReflectionTestUtils.setField(config, "allowedOrigins", origins);
        return ReflectionTestUtils.invokeMethod(config, "parseOrigins");
    }
}
