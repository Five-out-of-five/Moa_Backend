package com.fiveoutoffive.moa_backend.auth;

import com.fiveoutoffive.moa_backend.account.domain.model.SocialProvider;
import com.fiveoutoffive.moa_backend.auth.application.result.OAuthLoginResult;
import com.fiveoutoffive.moa_backend.auth.application.usecase.AuthUseCase;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.security.GlobalJwtProvider;
import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 01. Auth · 02. Account (API 12개 중 REST 10개 + 소셜 콜백 처리 로직).
 *
 * <p>소셜 제공자와의 왕복은 MockMvc 로 재현할 수 없어서, 콜백이 부르는 {@link AuthUseCase#handleOAuthLogin} 을 직접 호출해
 * 가입 티켓·기존 회원 로그인·차단 규칙을 확인한다. 그 뒤 흐름(가입 → 재발급 → 로그아웃)은 실제 API 로 거친다.
 */
@DisplayName("01. Auth · 02. Account")
class AuthAccountApiTest extends Be2ApiTestSupport {

    @Autowired
    private AuthUseCase authUseCase;

    @Autowired
    private GlobalJwtProvider jwtProvider;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    // ---------- 도우미 ----------

    private String origin() {
        return allowedOrigins.split(",")[0].trim();
    }

    private String uid() {
        return UUID.randomUUID().toString();
    }

    private String email() {
        return UUID.randomUUID() + "@be1.test";
    }

    private String ticketOf(OAuthLoginResult result) {
        assertThat(result.redirectPath()).startsWith("/signup?ticket=");
        return result.redirectPath().substring("/signup?ticket=".length());
    }

    private MockHttpServletResponse perform(MockHttpServletRequestBuilder req) throws Exception {
        MockHttpServletResponse res = mockMvc.perform(req).andReturn().getResponse();
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return res;
    }

    private MockHttpServletResponse signup(String ticket, String name, boolean terms, boolean privacy) throws Exception {
        return perform(MockMvcRequestBuilders.post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"ticket":"%s","name":"%s","termsAgreed":%s,"privacyAgreed":%s}""".formatted(ticket, name, terms, privacy)
                        .getBytes(StandardCharsets.UTF_8)));
    }

    /** refresh·logout 은 refreshToken 쿠키 + 허용된 Origin 으로 보낸다. */
    private MockHttpServletResponse cookiePost(String path, String refreshToken, String originHeader) throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.post("/api/v1/auth" + path);
        if (refreshToken != null) {
            req.cookie(new Cookie(GlobalJwtProvider.REFRESH_TOKEN_COOKIE, refreshToken));
        }
        if (originHeader != null) {
            req.header("Origin", originHeader);
        }
        return perform(req);
    }

    private String refreshCookieValue(MockHttpServletResponse res) {
        Cookie cookie = res.getCookie(GlobalJwtProvider.REFRESH_TOKEN_COOKIE);
        assertThat(cookie).as("refreshToken 쿠키가 있어야 한다").isNotNull();
        return cookie.getValue();
    }

    private Response body(MockHttpServletResponse res) throws Exception {
        return new Response(res.getStatus(), res.getContentAsString());
    }

    private String errorCodeOf(Runnable action) {
        try {
            action.run();
        } catch (BusinessException e) {
            return e.getErrorCode().getCode();
        }
        throw new AssertionError("BusinessException 이 나야 한다");
    }

    /** 가입까지 마친 회원의 액세스 토큰과 리프레시 토큰. */
    private record Joined(Member member, String refreshToken) {
    }

    private Joined join(String name) throws Exception {
        String ticket = ticketOf(authUseCase.handleOAuthLogin(SocialProvider.GOOGLE, uid(), email(), name));
        MockHttpServletResponse res = signup(ticket, name, true, true);
        Response r = body(res).ok();
        String access = r.read("$.data.accessToken");
        Long id = Long.valueOf(jwtProvider.getSubject(access));
        return new Joined(new Member(id, name, access), refreshCookieValue(res));
    }

    // ---------- 소셜 콜백 ----------

    @Nested
    @DisplayName("소셜 로그인 콜백 처리")
    class OAuthCallback {

        @Test
        @DisplayName("신규 회원은 회원을 만들지 않고 가입 티켓만 발급, 티켓으로 가입 화면 정보를 본다")
        void newUserGetsTicket() throws Exception {
            String email = email();
            String ticket = ticketOf(authUseCase.handleOAuthLogin(SocialProvider.KAKAO, uid(), email, "카카오이름"));

            Response info = get(null, "/auth/signup-tickets/" + ticket).ok();
            assertThat(info.<String>read("$.data.provider")).isEqualTo("KAKAO");
            assertThat(info.<String>read("$.data.email")).isEqualTo(email);
            assertThat(info.<String>read("$.data.name")).isEqualTo("카카오이름");
            assertThat(jdbc.queryForObject("SELECT count(*) FROM account WHERE email = ?", Integer.class, email)).isZero();
        }

        @Test
        @DisplayName("없는 티켓은 AUTH_SIGNUP_TICKET_INVALID")
        void invalidTicket() throws Exception {
            get(null, "/auth/signup-tickets/nothing").fails(400, "AUTH_SIGNUP_TICKET_INVALID");
        }

        @Test
        @DisplayName("이메일을 주지 않으면 AUTH_EMAIL_NOT_PROVIDED")
        void emailRequired() {
            assertThat(errorCodeOf(() -> authUseCase.handleOAuthLogin(SocialProvider.NAVER, uid(), null, "이름")))
                    .isEqualTo("AUTH_EMAIL_NOT_PROVIDED");
        }

        @Test
        @DisplayName("다른 제공자로 가입된 이메일이면 AUTH_EMAIL_ALREADY_REGISTERED")
        void emailUsedByOtherProvider() throws Exception {
            String email = email();
            String ticket = ticketOf(authUseCase.handleOAuthLogin(SocialProvider.GOOGLE, uid(), email, "구글"));
            body(signup(ticket, "구글", true, true)).ok();

            assertThat(errorCodeOf(() -> authUseCase.handleOAuthLogin(SocialProvider.KAKAO, uid(), email, "카카오")))
                    .isEqualTo("AUTH_EMAIL_ALREADY_REGISTERED");
        }

        @Test
        @DisplayName("기존 회원은 /auth/callback 으로 보내고 리프레시 토큰을 발급한다")
        void existingUser() throws Exception {
            String uid = uid();
            String email = email();
            body(signup(ticketOf(authUseCase.handleOAuthLogin(SocialProvider.GOOGLE, uid, email, "회원")), "회원", true, true)).ok();

            OAuthLoginResult again = authUseCase.handleOAuthLogin(SocialProvider.GOOGLE, uid, email, "회원");

            assertThat(again.redirectPath()).isEqualTo("/auth/callback");
            assertThat(again.refreshToken()).isNotBlank();
        }

        @Test
        @DisplayName("정지 회원은 AUTH_ACCOUNT_SUSPENDED")
        void suspended() throws Exception {
            String uid = uid();
            String email = email();
            body(signup(ticketOf(authUseCase.handleOAuthLogin(SocialProvider.GOOGLE, uid, email, "정지")), "정지", true, true)).ok();
            jdbc.update("UPDATE account SET status = 'SUSPENDED', suspended_until = (now() AT TIME ZONE 'UTC') + interval '3 days' WHERE email = ?", email);

            assertThat(errorCodeOf(() -> authUseCase.handleOAuthLogin(SocialProvider.GOOGLE, uid, email, "정지")))
                    .isEqualTo("AUTH_ACCOUNT_SUSPENDED");
        }
    }

    // ---------- 가입·토큰 ----------

    @Nested
    @DisplayName("가입·재발급·로그아웃")
    class Tokens {

        @Test
        @DisplayName("약관 미동의는 AUTH_TERMS_NOT_AGREED, 이름 특수문자는 COMMON_INVALID_INPUT")
        void signupRules() throws Exception {
            String ticket = ticketOf(authUseCase.handleOAuthLogin(SocialProvider.GOOGLE, uid(), email(), "이름"));

            body(signup(ticket, "이름", true, false)).fails(400, "AUTH_TERMS_NOT_AGREED");
            body(signup(ticket, "이름!", true, true)).fails(400, "COMMON_INVALID_INPUT");
        }

        @Test
        @DisplayName("가입하면 액세스 토큰(본문) + 리프레시 토큰(HttpOnly·SameSite=Strict 쿠키), 티켓은 한 번만 쓴다")
        void signupIssuesTokens() throws Exception {
            String ticket = ticketOf(authUseCase.handleOAuthLogin(SocialProvider.GOOGLE, uid(), email(), "새회원"));

            MockHttpServletResponse res = signup(ticket, "새회원", true, true);

            Response r = body(res).ok();
            assertThat(r.<String>read("$.data.accessToken")).isNotBlank();
            assertThat(r.<String>read("$.data.accessTokenExpiresAt")).contains("+09:00");
            String setCookie = res.getHeader("Set-Cookie");
            assertThat(setCookie).contains("refreshToken=").contains("HttpOnly").contains("SameSite=Strict");

            Response me = get(new Member(0L, "", r.read("$.data.accessToken")), "/accounts/me").ok();
            assertThat(me.<String>read("$.data.name")).isEqualTo("새회원");
            assertThat(me.<String>read("$.data.role")).isEqualTo("USER");
            assertThat(me.<String>read("$.data.toneMode")).isEqualTo("POLISH");

            body(signup(ticket, "새회원", true, true)).fails(400, "AUTH_SIGNUP_TICKET_INVALID");
        }

        @Test
        @DisplayName("재발급하면 리프레시 토큰도 교체된다. 저장되지 않은 토큰·쿠키 없음은 AUTH_REFRESH_TOKEN_INVALID")
        void refresh() throws Exception {
            Joined joined = join("재발급");

            MockHttpServletResponse res = cookiePost("/refresh", joined.refreshToken(), origin());
            body(res).ok();
            String rotated = refreshCookieValue(res);
            assertThat(rotated).isNotEqualTo(joined.refreshToken());

            body(cookiePost("/refresh", null, origin())).fails(401, "AUTH_REFRESH_TOKEN_INVALID");
            body(cookiePost("/refresh", "not.a.token", origin())).fails(401, "AUTH_REFRESH_TOKEN_INVALID");
        }

        @Test
        @DisplayName("같은 토큰으로 동시에(유예 시간 안에) 다시 재발급하면 같은 결과를 돌려준다")
        void refreshGrace() throws Exception {
            Joined joined = join("유예");

            Response first = body(cookiePost("/refresh", joined.refreshToken(), origin())).ok();
            Response second = body(cookiePost("/refresh", joined.refreshToken(), origin())).ok();

            assertThat(second.<String>read("$.data.accessToken")).isEqualTo(first.<String>read("$.data.accessToken"));
        }

        @Test
        @DisplayName("허용되지 않은 Origin 이면 refresh·logout 을 거절하고, 토큰은 그대로 쓸 수 있다")
        void originCheck() throws Exception {
            Joined joined = join("오리진");

            // CORS 필터가 컨트롤러보다 먼저 403 으로 막는다(본문은 공통 에러 형식이 아닌 "Invalid CORS request").
            assertThat(cookiePost("/refresh", joined.refreshToken(), "https://evil.kro.kr").getStatus()).isEqualTo(403);
            assertThat(cookiePost("/logout", joined.refreshToken(), "https://evil.kro.kr").getStatus()).isEqualTo(403);

            body(cookiePost("/refresh", joined.refreshToken(), origin())).ok();
        }

        @Test
        @DisplayName("로그아웃하면 쿠키를 지우고, 그 리프레시 토큰으로는 다시 재발급할 수 없다")
        void logout() throws Exception {
            Joined joined = join("로그아웃");

            MockHttpServletResponse res = cookiePost("/logout", joined.refreshToken(), origin());

            body(res).ok();
            assertThat(res.getHeader("Set-Cookie")).contains("refreshToken=").contains("Max-Age=0");
            body(cookiePost("/refresh", joined.refreshToken(), origin())).fails(401, "AUTH_REFRESH_TOKEN_INVALID");
        }

        @Test
        @DisplayName("토큰 없이 로그아웃해도 정상 처리한다")
        void logoutWithoutToken() throws Exception {
            body(cookiePost("/logout", null, origin())).ok();
        }
    }

    // ---------- 계정 ----------

    @Nested
    @DisplayName("계정 설정")
    class Account {

        @Test
        @DisplayName("기본 이름 수정, 특수문자는 거절")
        void updateName() throws Exception {
            Member me = newMember("원래이름");

            Response ok = patch(me, "/accounts/me", """
                    {"name":"바뀐 이름"}""").ok();
            assertThat(ok.<String>read("$.data.name")).isEqualTo("바뀐 이름");
            patch(me, "/accounts/me", """
                    {"name":"이름@"}""").fails(400, "COMMON_INVALID_INPUT");
        }

        @Test
        @DisplayName("톤앤매너 강도 변경")
        void aiSettings() throws Exception {
            Member me = newMember("회원");

            Response ok = patch(me, "/accounts/me/ai-settings", """
                    {"toneMode":"OFF"}""").ok();
            assertThat(ok.<String>read("$.data.toneMode")).isEqualTo("OFF");
            patch(me, "/accounts/me/ai-settings", """
                    {"toneMode":"STRONG"}""").fails(400, "COMMON_INVALID_INPUT");
        }

        @Test
        @DisplayName("마지막 방 저장은 멤버인 방만")
        void lastRoom() throws Exception {
            Member me = newMember("회원");
            Long mine = newRoom(me);
            Long others = newRoom(newMember("남"));

            put(me, "/accounts/me/last-room", """
                    {"roomId":%d}""".formatted(mine)).ok();
            assertThat(get(me, "/accounts/me").ok().id("$.data.lastRoomId")).isEqualTo(mine);

            put(me, "/accounts/me/last-room", """
                    {"roomId":%d}""".formatted(others)).fails(403, "ROOM_NOT_MEMBER");
        }

        @Test
        @DisplayName("방장으로 있는 방이 있으면 탈퇴할 수 없다")
        void ownerCannotWithdraw() throws Exception {
            Member owner = newMember("방장");
            Long roomId = newRoom(owner);

            Response check = get(owner, "/accounts/me/withdrawal-check").ok();
            assertThat(check.<Boolean>read("$.data.withdrawable")).isFalse();
            assertThat(check.id("$.data.ownedRooms[0].roomId")).isEqualTo(roomId);

            send(owner, HttpMethod.DELETE, "/accounts/me", """
                    {"confirmText":"탈퇴합니다"}""").fails(409, "ACCOUNT_OWNED_ROOM_EXISTS");
        }

        @Test
        @DisplayName("확인 문구가 다르면 ACCOUNT_WITHDRAW_CONFIRM_MISMATCH")
        void confirmText() throws Exception {
            Member me = newMember("회원");

            send(me, HttpMethod.DELETE, "/accounts/me", """
                    {"confirmText":"탈퇴"}""").fails(400, "ACCOUNT_WITHDRAW_CONFIRM_MISMATCH");
        }

        @Test
        @DisplayName("탈퇴하면 방에서 WITHDRAWN 으로 보이고, 재발급·같은 소셜 계정 재로그인이 막힌다")
        void withdraw() throws Exception {
            String uid = uid();
            String email = email();
            MockHttpServletResponse signed = signup(ticketOf(authUseCase.handleOAuthLogin(SocialProvider.GOOGLE, uid, email, "떠날회원")), "떠날회원", true, true);
            String access = body(signed).ok().read("$.data.accessToken");
            String refresh = refreshCookieValue(signed);
            Member me = new Member(Long.valueOf(jwtProvider.getSubject(access)), "떠날회원", access);
            Member owner = newMember("방장");
            Long roomId = newRoom(owner, me);

            assertThat(get(me, "/accounts/me/withdrawal-check").ok().<Boolean>read("$.data.withdrawable")).isTrue();
            send(me, HttpMethod.DELETE, "/accounts/me", """
                    {"confirmText":"탈퇴합니다"}""").ok();

            Response profile = get(owner, "/rooms/" + roomId + "/members/" + me.id()).ok();
            assertThat(profile.<String>read("$.data.memberStatus")).isEqualTo("WITHDRAWN");
            assertThat(profile.<String>read("$.data.displayName")).isNull();
            body(cookiePost("/refresh", refresh, origin())).fails(401, "AUTH_REFRESH_TOKEN_INVALID");
            assertThat(errorCodeOf(() -> authUseCase.handleOAuthLogin(SocialProvider.GOOGLE, uid, email, "떠날회원")))
                    .isEqualTo("AUTH_REJOIN_RESTRICTED");
        }
    }
}
