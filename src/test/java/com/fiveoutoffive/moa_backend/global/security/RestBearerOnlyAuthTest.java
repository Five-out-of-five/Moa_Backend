package com.fiveoutoffive.moa_backend.global.security;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * REST 인증은 Authorization: Bearer 헤더만 받는다. (11/12 회의 결정, API 명세 공통 규칙 '인증')
 *
 * <p>accessToken 쿠키로 인증되면 같은 kro.kr 사이트에서 보낸 폼 요청이 사용자 권한으로 실행될 수 있다(CSRF).
 */
@DisplayName("REST 인증: Bearer 만")
class RestBearerOnlyAuthTest extends Be2ApiTestSupport {

    private MockHttpServletResponse perform(MockHttpServletRequestBuilder req) throws Exception {
        MockHttpServletResponse res = mockMvc.perform(req).andReturn().getResponse();
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return res;
    }

    @Test
    @DisplayName("accessToken 쿠키만 있으면 인증되지 않는다 (401 COMMON_UNAUTHORIZED)")
    void cookieOnlyIsRejected() throws Exception {
        Member me = newMember("회원");

        MockHttpServletResponse res = perform(MockMvcRequestBuilders.get("/api/v1/rooms")
                .cookie(new Cookie(GlobalJwtProvider.ACCESS_TOKEN_COOKIE, me.token())));

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(res.getContentAsString()).contains("COMMON_UNAUTHORIZED");
    }

    @Test
    @DisplayName("바디 없는 변경 요청도 쿠키만으로는 실행되지 않는다")
    void cookieOnlyCannotChangeState() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);

        MockHttpServletResponse res = perform(MockMvcRequestBuilders.post("/api/v1/rooms/" + roomId + "/briefing/check")
                .cookie(new Cookie(GlobalJwtProvider.ACCESS_TOKEN_COOKIE, owner.token())));

        assertThat(res.getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("Bearer 헤더면 인증된다")
    void bearerIsAccepted() throws Exception {
        Member me = newMember("회원");

        get(me, "/rooms").ok();
    }

    @Test
    @DisplayName("Bearer 가 있으면 깨진 accessToken 쿠키가 함께 와도 무시한다")
    void brokenCookieIsIgnoredWithBearer() throws Exception {
        Member me = newMember("회원");

        MockHttpServletResponse res = perform(MockMvcRequestBuilders.get("/api/v1/rooms")
                .header("Authorization", "Bearer " + me.token())
                .cookie(new Cookie(GlobalJwtProvider.ACCESS_TOKEN_COOKIE, "broken.token.value")));

        assertThat(res.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("공개 경로는 깨진 accessToken 쿠키가 있어도 필터에서 401 로 막지 않는다 (/ws 핸드셰이크와 같은 경우)")
    void brokenCookieDoesNotBlockPublicPath() throws Exception {
        MockHttpServletResponse res = perform(MockMvcRequestBuilders.get("/v3/api-docs")
                .cookie(new Cookie(GlobalJwtProvider.ACCESS_TOKEN_COOKIE, "broken.token.value")));

        assertThat(res.getStatus()).isNotEqualTo(401);
    }
}
