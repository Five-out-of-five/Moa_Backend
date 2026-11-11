package com.fiveoutoffive.moa_backend.integration;

import com.fiveoutoffive.moa_backend.integration.application.service.NotionClient;
import com.fiveoutoffive.moa_backend.integration.application.service.SlackSender;
import com.fiveoutoffive.moa_backend.integration.infrastructure.crypto.IntegrationCipher;
import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.HttpClientErrorException;
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

    static final String WEBHOOK = "https://hooks.slack.com/services/T0000000/B0000000/abcdefghijklmnop";

    @MockitoBean
    NotionClient notionClient;

    @MockitoBean
    SlackSender slackSender;

    @Autowired
    IntegrationCipher cipher;

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
        assertThat(res.getRedirectedUrl()).contains("notion=error").contains("INTEGRATION_NOTION_STATE_INVALID");
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
        assertThat(res.getRedirectedUrl()).contains("/rooms/" + roomId + "/settings/integrations").contains("notion=connected");

        Response connected = get(owner, notion).ok();
        assertThat(connected.<Boolean>read("$.data.connected")).isTrue();
        assertThat(connected.<String>read("$.data.workspaceName")).isEqualTo("우리 팀");

        var reused = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/integrations/notion/callback")
                .param("code", "good-code").param("state", state)).andReturn().getResponse();
        assertThat(reused.getRedirectedUrl()).contains("INTEGRATION_NOTION_STATE_INVALID");
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

        put(owner, notion, "{\"targetDatabaseId\":\"db-1\",\"exportMode\":\"ON_SESSION_CLOSE\"}").ok();

        Response res = get(owner, notion).ok();
        assertThat(res.<String>read("$.data.exportMode")).isEqualTo("ON_SESSION_CLOSE");
        assertThat(res.<String>read("$.data.targetDatabase.id")).isEqualTo("db-1");
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
}
