package com.fiveoutoffive.moa_backend.integration;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 슬랙 발송과 노션 OAuth 는 외부 서비스가 필요해서 여기서는 다루지 않는다.
 * 연결 정보 저장·조회·해제와 권한, 연결 전 상태만 확인한다.
 */
@DisplayName("16. Integration")
class IntegrationApiTest extends Be2ApiTestSupport {

    static final String WEBHOOK = "https://hooks.slack.com/services/T0000000/B0000000/abcdefghijklmnop";

    Member owner;
    Member member;
    Long roomId;
    String slack;
    String notion;

    @BeforeEach
    void setUp() throws Exception {
        owner = newMember("방장");
        member = newMember("멤버");
        roomId = newRoom(owner, member);
        slack = "/rooms/" + roomId + "/integrations/slack";
        notion = "/rooms/" + roomId + "/integrations/notion";
    }

    @Test
    @DisplayName("연동 설정은 방장만 볼 수 있다")
    void ownerOnly() throws Exception {
        get(member, slack).fails(403, "ROOM_OWNER_ONLY");
        put(member, slack, "{\"webhookUrl\":\"%s\",\"eventTypes\":[\"VOTE_CREATED\"]}".formatted(WEBHOOK)).fails(403, "ROOM_OWNER_ONLY");
        get(member, notion).fails(403, "ROOM_OWNER_ONLY");
        post(member, notion + "/authorize").fails(403, "ROOM_OWNER_ONLY");
    }

    @Test
    @DisplayName("슬랙: 등록 → 조회(주소는 가림) → 해제, 두 번째 해제는 INTEGRATION_NOT_FOUND")
    void slackLifecycle() throws Exception {
        assertThat(get(owner, slack).ok().<Boolean>read("$.data.connected")).isFalse();

        Response saved = put(owner, slack, """
                {"webhookUrl":"%s","eventTypes":["VOTE_CREATED","SESSION_SUMMARY"]}""".formatted(WEBHOOK)).ok();
        assertThat(saved.<Boolean>read("$.data.connected")).isTrue();
        assertThat(saved.<List<String>>read("$.data.eventTypes")).containsExactlyInAnyOrder("VOTE_CREATED", "SESSION_SUMMARY");
        assertThat(saved.<String>read("$.data.webhookUrlMasked")).doesNotContain("abcdefghijklmnop");

        byte[] stored = jdbc.queryForObject("SELECT webhook_url_enc FROM slack_integration WHERE room_id = ?", byte[].class, roomId);
        assertThat(new String(stored, java.nio.charset.StandardCharsets.UTF_8)).doesNotContain("hooks.slack.com");

        delete(owner, slack).ok();
        assertThat(get(owner, slack).ok().<Boolean>read("$.data.connected")).isFalse();
        delete(owner, slack).fails(404, "INTEGRATION_NOT_FOUND");
    }

    @Test
    @DisplayName("슬랙: 웹훅 주소 형식이 아니면 거절")
    void slackInvalidUrl() throws Exception {
        put(owner, slack, """
                {"webhookUrl":"https://example.com/not-slack","eventTypes":["VOTE_CREATED"]}""").fails(400, "INTEGRATION_SLACK_URL_INVALID");
        post(owner, slack + "/test", """
                {"webhookUrl":"https://example.com/not-slack"}""").fails(400, "INTEGRATION_SLACK_URL_INVALID");
    }

    @Test
    @DisplayName("슬랙: 발송 이력은 비어 있으면 빈 페이지")
    void slackDeliveries() throws Exception {
        Response res = get(owner, slack + "/deliveries").ok();

        assertThat(res.<List<Object>>read("$.data.content")).isEmpty();
    }

    @Test
    @DisplayName("노션: 연결 전에는 connected=false, 연결 시작하면 노션 인증 주소를 준다")
    void notionAuthorize() throws Exception {
        assertThat(get(owner, notion).ok().<Boolean>read("$.data.connected")).isFalse();

        String url = post(owner, notion + "/authorize").ok().read("$.data.authorizeUrl");

        assertThat(url).startsWith("https://api.notion.com/").contains("state=");
    }

    @Test
    @DisplayName("노션: 연결 전에는 데이터베이스 목록·설정·내보내기·해제가 INTEGRATION_NOT_FOUND")
    void notionNotConnected() throws Exception {
        get(owner, notion + "/databases").fails(404, "INTEGRATION_NOT_FOUND");
        put(owner, notion, "{\"targetDatabaseId\":\"db\",\"exportMode\":\"MANUAL\"}").fails(404, "INTEGRATION_NOT_FOUND");
        delete(owner, notion).fails(404, "INTEGRATION_NOT_FOUND");
    }

    @Test
    @DisplayName("노션: 내보내기 이력은 연결 전에도 빈 목록(명세에 에러 없음)")
    void notionExportHistoryBeforeConnect() throws Exception {
        Response res = get(owner, notion + "/exports").ok();

        assertThat(res.<List<Object>>read("$.data.content")).isEmpty();
    }
}
