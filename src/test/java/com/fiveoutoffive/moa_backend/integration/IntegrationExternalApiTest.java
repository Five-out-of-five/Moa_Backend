package com.fiveoutoffive.moa_backend.integration;

import com.fiveoutoffive.moa_backend.integration.application.result.NotionDatabaseResponse;
import com.fiveoutoffive.moa_backend.integration.application.service.NotionClient;
import com.fiveoutoffive.moa_backend.integration.application.service.SlackSender;
import com.fiveoutoffive.moa_backend.integration.infrastructure.crypto.IntegrationCipher;
import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

/**
 * 슬랙·노션 외부 호출이 실패하거나 성공하는 경우. 실제 외부 서비스는 부르지 않도록 클라이언트를 가짜로 바꾼다.
 */
@DisplayName("16. Integration (외부 호출)")
class IntegrationExternalApiTest extends Be2ApiTestSupport {

    static final String SETTINGS = "{\"targetDatabaseId\":\"db-1\",\"exportMode\":\"ON_SESSION_CLOSE\"}";
    static final String WEBHOOK = "https://hooks.slack.com/services/T0000000/B0000000/abcdefghijklmnop";

    @MockitoBean
    NotionClient notionClient;

    @MockitoBean
    SlackSender slackSender;

    @Autowired
    IntegrationCipher cipher;

    @Value("${app.frontend-url}")
    String front;

    Member owner;
    Long roomId;
    String notion;

    @BeforeEach
    void setUp() {
        owner = newMember("방장");
        roomId = newRoom(owner);
        notion = "/rooms/" + roomId + "/integrations/notion";
    }

    private void connectNotion(String targetDatabaseId) {
        jdbc.update("""
                INSERT INTO notion_integration(room_id, access_token_enc, notion_workspace_name, target_database_id, created_by_account_id)
                VALUES (?, ?, '테스트 워크스페이스', ?, ?)
                """, roomId, cipher.encrypt("secret-token"), targetDatabaseId, owner.id());
    }

    private Long closedSession() throws Exception {
        Long sessionId = post(owner, "/rooms/" + roomId + "/sessions", "{\"title\":\"회고 1회차\"}").ok().id("$.data.sessionId");
        post(owner, "/rooms/" + roomId + "/idea-cards", "{\"cardType\":\"KEEP\",\"title\":\"좋았던 점\"}").ok();
        post(owner, "/rooms/" + roomId + "/sessions/" + sessionId + "/close").ok();
        return sessionId;
    }

    // ---------- 슬랙 ----------

    @Test
    @DisplayName("슬랙 테스트 발송: 실패하면 INTEGRATION_SLACK_TEST_FAILED, 성공하면 200")
    void slackTest() throws Exception {
        given(slackSender.send(eq(WEBHOOK), anyString())).willReturn(false);
        post(owner, "/rooms/" + roomId + "/integrations/slack/test", "{\"webhookUrl\":\"%s\"}".formatted(WEBHOOK))
                .fails(502, "INTEGRATION_SLACK_TEST_FAILED");

        given(slackSender.send(eq(WEBHOOK), anyString())).willReturn(true);
        post(owner, "/rooms/" + roomId + "/integrations/slack/test", "{\"webhookUrl\":\"%s\"}".formatted(WEBHOOK)).ok();
    }

    // ---------- 노션 연결(OAuth 콜백) ----------

    @Test
    @DisplayName("콜백: state 가 틀리면 로그인 없이도 프론트로 돌려보내며 STATE_INVALID 를 붙인다")
    void callbackInvalidState() throws Exception {
        var res = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/integrations/notion/callback")
                .param("code", "c").param("state", "wrong")).andReturn().getResponse();

        assertThat(res.getStatus()).isEqualTo(302);
        assertThat(res.getRedirectedUrl()).isEqualTo(front + "/workspaces/manage?notion=error&code=INTEGRATION_NOTION_STATE_INVALID");
    }

    @Test
    @DisplayName("콜백: 연결 시작 → 콜백 성공이면 연결되고, 같은 state 는 다시 쓸 수 없다")
    void callbackConnect() throws Exception {
        String url = post(owner, notion + "/authorize").ok().read("$.data.authorizeUrl");
        String state = url.replaceAll(".*[?&]state=([^&]+).*", "$1");
        given(notionClient.exchangeCode(eq("good-code"), anyString(), anyString(), anyString())).willReturn(
                JsonMapper.builder().build().readTree("{\"access_token\":\"tok\",\"workspace_id\":\"ws-1\",\"workspace_name\":\"우리 팀\"}"));

        var res = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/integrations/notion/callback")
                .param("code", "good-code").param("state", state)).andReturn().getResponse();
        assertThat(res.getRedirectedUrl()).isEqualTo(front + "/workspaces/manage?room=" + roomId + "&tab=integrations&notion=connected");

        Response connected = get(owner, notion).ok();
        assertThat(connected.<Boolean>read("$.data.connected")).isTrue();
        assertThat(connected.<String>read("$.data.workspaceName")).isEqualTo("우리 팀");

        var reused = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/integrations/notion/callback")
                .param("code", "good-code").param("state", state)).andReturn().getResponse();
        assertThat(reused.getRedirectedUrl()).isEqualTo(front + "/workspaces/manage?notion=error&code=INTEGRATION_NOTION_STATE_INVALID");
    }

    @Test
    @DisplayName("콜백: 노션 화면에서 취소하면(code 없이 error=access_denied) 400 이 아니라 연동 탭으로 돌려보내고 연결하지 않는다")
    void callbackCancelled() throws Exception {
        String url = post(owner, notion + "/authorize").ok().read("$.data.authorizeUrl");
        String state = url.replaceAll(".*[?&]state=([^&]+).*", "$1");

        var res = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/integrations/notion/callback")
                .param("error", "access_denied").param("state", state)).andReturn().getResponse();

        assertThat(res.getStatus()).isEqualTo(302);
        assertThat(res.getRedirectedUrl()).isEqualTo(front + "/workspaces/manage?room=" + roomId + "&tab=integrations&notion=cancelled");
        assertThat(get(owner, notion).ok().<Boolean>read("$.data.connected")).isFalse();
    }

    @Test
    @DisplayName("콜백: code·state 가 둘 다 없어도 400 이 아니라 STATE_INVALID 로 돌려보낸다")
    void callbackWithoutParams() throws Exception {
        var res = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/integrations/notion/callback")
                .param("error", "access_denied")).andReturn().getResponse();

        assertThat(res.getStatus()).isEqualTo(302);
        assertThat(res.getRedirectedUrl()).isEqualTo(front + "/workspaces/manage?notion=error&code=INTEGRATION_NOTION_STATE_INVALID");
    }

    @Test
    @DisplayName("콜백: 노션 토큰 발급이 실패하면 연동 탭으로 돌려보내며 AUTH_EXPIRED 를 붙이고 연결하지 않는다")
    void callbackTokenExchangeFails() throws Exception {
        String url = post(owner, notion + "/authorize").ok().read("$.data.authorizeUrl");
        String state = url.replaceAll(".*[?&]state=([^&]+).*", "$1");
        willThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).given(notionClient).exchangeCode(eq("bad-code"), anyString(), anyString(), anyString());

        var res = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/integrations/notion/callback")
                .param("code", "bad-code").param("state", state)).andReturn().getResponse();

        assertThat(res.getStatus()).isEqualTo(302);
        assertThat(res.getRedirectedUrl()).isEqualTo(front + "/workspaces/manage?room=" + roomId + "&tab=integrations&notion=error&code=INTEGRATION_NOTION_AUTH_EXPIRED");
        assertThat(get(owner, notion).ok().<Boolean>read("$.data.connected")).isFalse();
    }

    @Test
    @DisplayName("콜백: 연결을 시작한 뒤 방장이 아니게 됐으면 같은 방의 기본 탭으로 돌려보내며 ROOM_OWNER_ONLY 를 붙이고 연결하지 않는다")
    void callbackOwnerLost() throws Exception {
        String url = post(owner, notion + "/authorize").ok().read("$.data.authorizeUrl");
        String state = url.replaceAll(".*[?&]state=([^&]+).*", "$1");
        jdbc.update("UPDATE room_member SET role = 'MEMBER' WHERE room_id = ? AND account_id = ?", roomId, owner.id());

        var res = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/integrations/notion/callback")
                .param("code", "good-code").param("state", state)).andReturn().getResponse();

        assertThat(res.getStatus()).isEqualTo(302);
        assertThat(res.getRedirectedUrl()).isEqualTo(front + "/workspaces/manage?room=" + roomId + "&notion=error&code=ROOM_OWNER_ONLY");
        verify(notionClient, org.mockito.Mockito.never()).exchangeCode(anyString(), anyString(), anyString(), anyString());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notion_integration WHERE room_id = ?", Integer.class, roomId)).isZero();
    }

    // ---------- 노션 데이터베이스·설정 ----------

    @Test
    @DisplayName("데이터베이스 목록: 노션이 401 이면 AUTH_EXPIRED 이고, 연동 조회에 tokenExpired=true 가 남는다")
    void databasesExpired() throws Exception {
        connectNotion(null);
        willThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED)).given(notionClient).searchDatabases("secret-token");

        get(owner, notion + "/databases").fails(409, "INTEGRATION_NOTION_AUTH_EXPIRED");

        assertThat(get(owner, notion).ok().<Boolean>read("$.data.tokenExpired")).isTrue();
    }

    @Test
    @DisplayName("내보내기 설정 저장 후 조회에 반영된다")
    void updateSettings() throws Exception {
        connectNotion(null);
        given(notionClient.retrieveDatabase("secret-token", "db-1")).willReturn(new NotionDatabaseResponse("db-1", "팀 회고"));

        Response saved = put(owner, notion, SETTINGS).ok();
        assertThat(saved.<String>read("$.data.targetDatabase.title")).isEqualTo("팀 회고");

        Response res = get(owner, notion).ok();
        assertThat(res.<String>read("$.data.exportMode")).isEqualTo("ON_SESSION_CLOSE");
        assertThat(res.<String>read("$.data.targetDatabase.id")).isEqualTo("db-1");
        assertThat(res.<String>read("$.data.targetDatabase.title")).isEqualTo("팀 회고");
        assertThat(res.<String>read("$.data.workspaceName")).isEqualTo("테스트 워크스페이스");
    }

    @Test
    @DisplayName("내보내기 설정: 없거나 접근할 수 없는 DB 면 DATABASE_NOT_FOUND 이고 기존 설정이 남는다")
    void updateSettingsDatabaseNotFound() throws Exception {
        connectNotion("db-old");
        for (HttpStatus status : List.of(HttpStatus.BAD_REQUEST, HttpStatus.FORBIDDEN, HttpStatus.NOT_FOUND)) {
            willThrow(new HttpClientErrorException(status)).given(notionClient).retrieveDatabase("secret-token", "db-1");
            put(owner, notion, SETTINGS).fails(409, "INTEGRATION_NOTION_DATABASE_NOT_FOUND");
        }

        Response res = get(owner, notion).ok();
        assertThat(res.<String>read("$.data.targetDatabase.id")).isEqualTo("db-old");
        assertThat(res.<String>read("$.data.exportMode")).isEqualTo("MANUAL");
        assertThat(res.<Boolean>read("$.data.tokenExpired")).isFalse();
    }

    @Test
    @DisplayName("내보내기 설정: 노션이 401 이면 AUTH_EXPIRED 이고 만료 표시가 남는다")
    void updateSettingsExpired() throws Exception {
        connectNotion(null);
        willThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED)).given(notionClient).retrieveDatabase("secret-token", "db-1");

        put(owner, notion, SETTINGS).fails(409, "INTEGRATION_NOTION_AUTH_EXPIRED");

        assertThat(get(owner, notion).ok().<Boolean>read("$.data.tokenExpired")).isTrue();
    }

    @Test
    @DisplayName("내보내기 설정: 노션 일시 장애면 저장은 되고, 대상이 그대로면 이름을 유지하며 바뀌면 옛 이름을 남기지 않는다")
    void updateSettingsNotionUnavailable() throws Exception {
        connectNotion(null);
        given(notionClient.retrieveDatabase("secret-token", "db-1")).willReturn(new NotionDatabaseResponse("db-1", "팀 회고"));
        put(owner, notion, SETTINGS).ok();

        for (RestClientException e : List.of(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS),
                new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE), new ResourceAccessException("timeout"))) {
            willThrow(e).given(notionClient).retrieveDatabase(anyString(), anyString());
            put(owner, notion, "{\"targetDatabaseId\":\"db-1\",\"exportMode\":\"MANUAL\"}").ok();
        }
        Response same = get(owner, notion).ok();
        assertThat(same.<String>read("$.data.targetDatabase.title")).isEqualTo("팀 회고");
        assertThat(same.<String>read("$.data.exportMode")).isEqualTo("MANUAL");
        assertThat(same.<Boolean>read("$.data.tokenExpired")).isFalse();

        Response changed = put(owner, notion, "{\"targetDatabaseId\":\"db-2\",\"exportMode\":\"MANUAL\"}").ok();
        assertThat(changed.<String>read("$.data.targetDatabase.id")).isEqualTo("db-2");
        assertThat(changed.<String>read("$.data.targetDatabase.title")).isNull();
    }

    @Test
    @DisplayName("내보내기 설정: 노션에서 이름을 조회하는 사이 다시 연결돼도 새 토큰을 덮어쓰지 않고, 해제되면 되살리지 않는다")
    void updateSettingsKeepsReconnected() throws Exception {
        connectNotion(null);
        byte[] newToken = cipher.encrypt("new-token");
        duringLookup("UPDATE notion_integration SET access_token_enc = ? WHERE room_id = ?", newToken, roomId);
        put(owner, notion, SETTINGS).ok();
        assertThat(jdbc.queryForObject("SELECT access_token_enc FROM notion_integration WHERE room_id = ?", byte[].class, roomId)).isEqualTo(newToken);

        duringLookup("DELETE FROM notion_integration WHERE room_id = ?", roomId);
        put(owner, notion, SETTINGS).fails(404, "INTEGRATION_NOT_FOUND");
        assertThat(get(owner, notion).ok().<Boolean>read("$.data.connected")).isFalse();
    }

    /** 이름을 조회하는 동안 다른 요청이 먼저 커밋한 상황을 만든다. 다른 스레드라 저장 트랜잭션과 섞이지 않는다. */
    private void duringLookup(String sql, Object... args) {
        org.mockito.BDDMockito.willAnswer(call -> {
            java.util.concurrent.CompletableFuture.runAsync(() -> jdbc.update(sql, args)).get(5, java.util.concurrent.TimeUnit.SECONDS);
            return new NotionDatabaseResponse("db-1", "팀 회고");
        }).given(notionClient).retrieveDatabase(anyString(), anyString());
    }

    // ---------- 노션 내보내기 ----------

    @Test
    @DisplayName("내보내기: 대상 DB 미설정, 없는 회차·진행 중 회차, 연결 없음")
    void exportErrors() throws Exception {
        Long sessionId = closedSession();
        post(owner, notion + "/exports", "{\"sessionId\":%d}".formatted(sessionId)).fails(404, "INTEGRATION_NOT_FOUND");

        connectNotion(null);
        post(owner, notion + "/exports", "{\"sessionId\":%d}".formatted(sessionId)).fails(409, "INTEGRATION_NOTION_DATABASE_NOT_FOUND");

        jdbc.update("UPDATE notion_integration SET target_database_id = 'db-1' WHERE room_id = ?", roomId);
        post(owner, notion + "/exports", "{\"sessionId\":999999999}").fails(404, "SESSION_NOT_FOUND");
        Long running = post(owner, "/rooms/" + roomId + "/sessions", "{\"title\":\"진행 중\"}").ok().id("$.data.sessionId");
        post(owner, notion + "/exports", "{\"sessionId\":%d}".formatted(running)).fails(404, "SESSION_NOT_FOUND");
    }

    @Test
    @DisplayName("내보내기 성공 후 같은 회차를 다시 내보내면 기존 페이지를 갱신한다")
    void exportAndUpdate() throws Exception {
        connectNotion("db-1");
        Long sessionId = closedSession();
        given(notionClient.createPage(eq("secret-token"), eq("db-1"), eq("회고 1회차"), anyString())).willReturn("page-1");

        post(owner, notion + "/exports", "{\"sessionId\":%d}".formatted(sessionId)).ok();
        post(owner, notion + "/exports", "{\"sessionId\":%d}".formatted(sessionId)).ok();

        verify(notionClient).updatePage(eq("secret-token"), eq("page-1"), anyString());
        List<String> statuses = get(owner, notion + "/exports").ok().read("$.data.content[*].status");
        assertThat(statuses).containsExactly("SUCCESS");
    }

    @Test
    @DisplayName("내보내기 실패: 이력에 FAILED 가 남고, 401 이면 AUTH_EXPIRED 와 만료 표시")
    void exportFailures() throws Exception {
        connectNotion("db-1");
        Long sessionId = closedSession();

        willThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND)).given(notionClient).createPage(anyString(), anyString(), anyString(), anyString());
        post(owner, notion + "/exports", "{\"sessionId\":%d}".formatted(sessionId)).fails(409, "INTEGRATION_NOTION_DATABASE_NOT_FOUND");
        List<String> statuses = get(owner, notion + "/exports").ok().read("$.data.content[*].status");
        assertThat(statuses).containsExactly("FAILED");

        willThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED)).given(notionClient).createPage(anyString(), anyString(), anyString(), anyString());
        post(owner, notion + "/exports", "{\"sessionId\":%d}".formatted(sessionId)).fails(409, "INTEGRATION_NOTION_AUTH_EXPIRED");
        assertThat(get(owner, notion).ok().<Boolean>read("$.data.tokenExpired")).isTrue();
    }

    @Test
    @DisplayName("방장이 아니면 데이터베이스 목록·내보내기도 ROOM_OWNER_ONLY (500 이 아니어야 함)")
    void ownerOnly() throws Exception {
        Member member = newMember("멤버");
        Long room = newRoom(owner, member);
        String path = "/rooms/" + room + "/integrations/notion";

        get(member, path + "/databases").fails(403, "ROOM_OWNER_ONLY");
        post(member, path + "/exports", "{\"sessionId\":1}").fails(403, "ROOM_OWNER_ONLY");
        get(member, path + "/exports").fails(403, "ROOM_OWNER_ONLY");
        delete(member, path).fails(403, "ROOM_OWNER_ONLY");
        post(member, "/rooms/" + room + "/integrations/slack/test", "{\"webhookUrl\":\"%s\"}".formatted(WEBHOOK)).fails(403, "ROOM_OWNER_ONLY");
        get(member, "/rooms/" + room + "/integrations/slack/deliveries").fails(403, "ROOM_OWNER_ONLY");
        delete(member, "/rooms/" + room + "/integrations/slack").fails(403, "ROOM_OWNER_ONLY");
    }

    // ---------- 동시성 (IN-1, IN-2) ----------

    private static void waitUntil(java.util.function.BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10_000;
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError("10초 안에 조건이 맞지 않음");
            }
            Thread.sleep(50);
        }
    }

    @Test
    @DisplayName("슬랙 발송 중에 방장이 설정을 바꿔도 발송 결과가 바뀐 설정을 덮어쓰지 않는다")
    void slackDeliveryKeepsNewSettings() throws Exception {
        String slack = "/rooms/" + roomId + "/integrations/slack";
        put(owner, slack, "{\"webhookUrl\":\"%s\",\"eventTypes\":[\"SESSION_SUMMARY\",\"VOTE_CREATED\"]}".formatted(WEBHOOK)).ok();
        java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicBoolean sending = new java.util.concurrent.atomic.AtomicBoolean();
        org.mockito.BDDMockito.willAnswer(call -> {
            sending.set(true);
            release.await(10, java.util.concurrent.TimeUnit.SECONDS);
            return false;
        }).given(slackSender).send(anyString(), anyString());

        Long sessionId = post(owner, "/rooms/" + roomId + "/sessions", "{\"title\":\"회차\"}").ok().id("$.data.sessionId");
        post(owner, "/rooms/" + roomId + "/sessions/" + sessionId + "/close").ok();
        waitUntil(sending::get);
        put(owner, slack, "{\"eventTypes\":[\"VOTE_CREATED\"]}").ok();
        release.countDown();
        waitUntil(() -> jdbc.queryForObject("SELECT count(*) FROM slack_delivery_log", Integer.class) > 0);

        assertThat(jdbc.queryForObject("SELECT event_types::text FROM slack_integration WHERE room_id = ?", String.class, roomId)).isEqualTo("{VOTE_CREATED}");
        assertThat(jdbc.queryForObject("SELECT consecutive_fail_count FROM slack_integration WHERE room_id = ?", Integer.class, roomId)).isEqualTo(1);
    }

    @Test
    @DisplayName("노션 내보내기를 동시에 두 번 눌러도 페이지는 하나만 만들고 둘 다 성공한다")
    void exportTwiceTogether() throws Exception {
        connectNotion("db-1");
        Long sessionId = closedSession();
        org.mockito.BDDMockito.willAnswer(call -> {
            Thread.sleep(300);
            return "page-1";
        }).given(notionClient).createPage(eq("secret-token"), eq("db-1"), eq("회고 1회차"), anyString());
        String body = "{\"sessionId\":%d}".formatted(sessionId);

        List<Integer> statuses = sendTogether(() -> post(owner, notion + "/exports", body), () -> post(owner, notion + "/exports", body));

        assertThat(statuses).containsExactly(200, 200);
        verify(notionClient, org.mockito.Mockito.times(1)).createPage(anyString(), anyString(), anyString(), anyString());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notion_export_log WHERE session_id = ?", Integer.class, sessionId)).isEqualTo(1);
    }
}
