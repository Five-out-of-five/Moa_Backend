package com.fiveoutoffive.moa_backend.global;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API 명세 공통 규칙: 응답 형식, HTTP 상태, 공통 에러, 페이지네이션, 날짜·시간.
 */
@DisplayName("공통 응답·에러 형식")
class CommonResponseApiTest extends Be2ApiTestSupport {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Test
    @DisplayName("성공: { success: true, data }, data 없음은 data=null 과 200")
    void successFormat() throws Exception {
        Member me = newMember("회원");

        Response res = get(me, "/accounts/me").ok();
        assertThat(res.<Boolean>read("$.success")).isTrue();
        assertThat(res.<String>read("$.data.name")).isEqualTo("회원");

        Long roomId = newRoom(me);
        Response empty = put(me, "/accounts/me/last-room", """
                {"roomId":%d}""".formatted(roomId)).ok();
        assertThat(empty.<Object>read("$.data")).isNull();
    }

    @Test
    @DisplayName("입력값 오류: 400 COMMON_INVALID_INPUT + errors[{ field, reason }]")
    void validationFormat() throws Exception {
        Member me = newMember("회원");

        Response res = patch(me, "/accounts/me", """
                {"name":""}""").fails(400, "COMMON_INVALID_INPUT");

        assertThat(res.<Boolean>read("$.success")).isFalse();
        List<Map<String, Object>> errors = res.read("$.error.errors");
        assertThat(errors).isNotEmpty();
        assertThat(errors.get(0)).containsKeys("field", "reason");
        assertThat(errors.get(0).get("field")).isEqualTo("name");
    }

    @Test
    @DisplayName("JSON 이 깨지면 400 COMMON_INVALID_INPUT")
    void brokenJson() throws Exception {
        patch(newMember("회원"), "/accounts/me", "{name:").fails(400, "COMMON_INVALID_INPUT");
    }

    @Test
    @DisplayName("토큰 없음·위조 토큰은 401 COMMON_UNAUTHORIZED")
    void unauthorized() throws Exception {
        get(null, "/accounts/me").fails(401, "COMMON_UNAUTHORIZED");
        get(new Member(0L, "", "forged.token.value"), "/accounts/me").fails(401, "COMMON_UNAUTHORIZED");
    }

    @Test
    @DisplayName("만료된 토큰은 401 AUTH_ACCESS_TOKEN_EXPIRED (프론트가 재발급 후 재시도)")
    void expired() throws Exception {
        Member me = newMember("회원");
        String expired = Jwts.builder().subject(String.valueOf(me.id())).claim("role", "USER")
                .issuedAt(new Date(System.currentTimeMillis() - 7_200_000L))
                .expiration(new Date(System.currentTimeMillis() - 1_000L))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();

        get(new Member(me.id(), "", expired), "/accounts/me").fails(401, "AUTH_ACCESS_TOKEN_EXPIRED");
    }

    @Test
    @DisplayName("없는 경로는 404 공통 에러 형식")
    void notFoundPath() throws Exception {
        Response res = get(newMember("회원"), "/no-such-api");

        assertThat(res.status()).isEqualTo(404);
        assertThat(res.<Boolean>read("$.success")).isFalse();
        assertThat(res.errorCode()).isNotNull();
    }

    @Test
    @DisplayName("목록 size 는 최대 100 으로 자르고 PageResponse 형식으로 준다")
    void pagination() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);

        Response res = get(owner, "/rooms/" + roomId + "/join-requests?size=500").ok();

        assertThat(res.<Integer>read("$.data.size")).isEqualTo(100);
        assertThat(res.<Integer>read("$.data.page")).isZero();
        assertThat(res.<Object>read("$.data.content")).isNotNull();
        assertThat(res.<Object>read("$.data.totalElements")).isNotNull();
        assertThat(res.<Object>read("$.data.totalPages")).isNotNull();
        assertThat(res.<Object>read("$.data.hasNext")).isNotNull();
    }

    @Test
    @DisplayName("응답 시각은 한국 시간 오프셋(+09:00)이 붙은 ISO-8601")
    void kstTime() throws Exception {
        Response res = get(newMember("회원"), "/accounts/me").ok();

        assertThat(res.<String>read("$.data.createdAt")).endsWith("+09:00");
    }
}
