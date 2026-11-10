package com.fiveoutoffive.moa_backend.ai;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 테스트에서는 AI 서버를 닫힌 포트로 지정하므로 항상 "AI 서버 꺼짐" 상태다.
 * 실제 AI 응답이 필요한 흐름(요약 생성 성공 등)은 Python 서버가 생긴 뒤 따로 시험한다.
 */
@DisplayName("15. AI")
class AiApiTest extends Be2ApiTestSupport {

    Member owner;
    Member member;
    Long roomId;
    String ai;

    @BeforeEach
    void setUp() throws Exception {
        owner = newMember("방장");
        member = newMember("멤버");
        roomId = newRoom(owner, member);
        ai = "/rooms/" + roomId + "/ai";
    }

    private void cards(int count) throws Exception {
        post(owner, "/rooms/" + roomId + "/sessions", "{\"title\":\"회차\"}").ok();
        for (int i = 1; i <= count; i++) {
            post(owner, "/rooms/" + roomId + "/idea-cards", """
                    {"cardType":"KEEP","title":"카드 %d"}""".formatted(i)).ok();
        }
    }

    private Long insertSummary(Member requester, String content) {
        return jdbc.queryForObject("""
                INSERT INTO ai_summary(room_id, scope_type, content, requested_by_account_id) VALUES (?, 'ALL', ?, ?) RETURNING id
                """, Long.class, roomId, content, requester.id());
    }

    private void useUp(String feature, int used) {
        jdbc.update("""
                INSERT INTO ai_usage_quota(room_id, feature, quota_date, used_count)
                VALUES (?, ?, (now() AT TIME ZONE 'Asia/Seoul')::date, ?)
                """, roomId, feature, used);
    }

    @Test
    @DisplayName("한도 조회: 명세의 시작값이고 자동 충돌 감지(CONFLICT)는 내려주지 않는다")
    void quota() throws Exception {
        Response res = get(member, ai + "/quota").ok();

        List<Map<String, Object>> features = res.read("$.data.features");
        Map<String, Object> limits = new java.util.HashMap<>();
        features.forEach(f -> limits.put((String) f.get("feature"), f.get("limit")));
        assertThat(limits).containsEntry("ASSISTANT", 30).containsEntry("SUMMARY", 10)
                .containsEntry("COMPROMISE", 10).containsEntry("LAYOUT", 5).containsEntry("TONE", 100)
                .doesNotContainKey("CONFLICT");
    }

    @Test
    @DisplayName("카드가 3건 미만이면 요약하지 않는다")
    void summaryNeedsThreeCards() throws Exception {
        cards(2);

        post(owner, ai + "/summaries", "{\"scopeType\":\"ALL\"}").fails(409, "AI_NOT_ENOUGH_CARDS");
    }

    @Test
    @DisplayName("AI 서버가 꺼져 있으면 요약은 AI_UNAVAILABLE, 한도는 차감하지 않고 실패 기록만 남긴다")
    void summaryUnavailable() throws Exception {
        cards(3);

        post(owner, ai + "/summaries", "{\"scopeType\":\"ALL\"}").fails(503, "AI_UNAVAILABLE");

        List<Integer> used = get(owner, ai + "/quota").ok().read("$.data.features[?(@.feature=='SUMMARY')].used");
        assertThat(used).containsExactly(0);
        Integer failed = jdbc.queryForObject("SELECT count(*) FROM ai_call_log WHERE room_id = ? AND feature = 'SUMMARY' AND status = 'FAILED'",
                Integer.class, roomId);
        assertThat(failed).isEqualTo(1);
    }

    @Test
    @DisplayName("오늘 한도를 다 쓰면 AI_QUOTA_EXCEEDED(429)")
    void summaryQuotaExceeded() throws Exception {
        cards(3);
        useUp("SUMMARY", 10);

        post(owner, ai + "/summaries", "{\"scopeType\":\"ALL\"}").fails(429, "AI_QUOTA_EXCEEDED");
    }

    @Test
    @DisplayName("회차를 골라 요약: 없는 회차·진행 중 회차·다른 방 회차는 SESSION_NOT_FOUND")
    void summarySessionScope() throws Exception {
        cards(3);
        Long inProgress = get(owner, "/rooms/" + roomId + "/sessions/current").ok().id("$.data.sessionId");
        Member stranger = newMember("다른방장");
        Long otherRoom = newRoom(stranger);
        Long otherSession = post(stranger, "/rooms/" + otherRoom + "/sessions", "{\"title\":\"남의 회차\"}").ok().id("$.data.sessionId");
        post(stranger, "/rooms/" + otherRoom + "/sessions/" + otherSession + "/close").ok();

        post(owner, ai + "/summaries", "{\"scopeType\":\"SESSIONS\",\"sessionIds\":[999999999]}").fails(404, "SESSION_NOT_FOUND");
        post(owner, ai + "/summaries", "{\"scopeType\":\"SESSIONS\",\"sessionIds\":[%d]}".formatted(inProgress)).fails(404, "SESSION_NOT_FOUND");
        post(owner, ai + "/summaries", "{\"scopeType\":\"SESSIONS\",\"sessionIds\":[%d]}".formatted(otherSession)).fails(404, "SESSION_NOT_FOUND");
        post(owner, ai + "/summaries", "{\"scopeType\":\"SESSIONS\"}").fails(404, "SESSION_NOT_FOUND");
        post(owner, ai + "/summaries", "{}").fails(400, "COMMON_INVALID_INPUT");
    }

    @Test
    @DisplayName("없는 요약을 카드로 저장하면 AI_SUMMARY_NOT_FOUND")
    void saveUnknownSummary() throws Exception {
        post(owner, "/rooms/" + roomId + "/sessions", "{\"title\":\"회차\"}").ok();

        post(owner, ai + "/summaries/999999999/save-as-card", "{\"cardType\":\"AGENDA\",\"title\":\"카드\"}").fails(404, "AI_SUMMARY_NOT_FOUND");
    }

    @Test
    @DisplayName("어시스턴트·톤 점검도 오늘 한도를 다 쓰면 AI_QUOTA_EXCEEDED")
    void otherQuotas() throws Exception {
        useUp("ASSISTANT", 30);
        useUp("TONE", 100);

        post(member, ai + "/assistant/messages", "{\"question\":\"질문\"}").fails(429, "AI_QUOTA_EXCEEDED");
        post(member, ai + "/tone-checks", """
                {"text":"안녕하세요","target":"IDEA_CARD"}""").fails(429, "AI_QUOTA_EXCEEDED");
    }

    @Test
    @DisplayName("요청 값 검사: 질문 200자 초과, 톤 점검 2000자 초과")
    void validation() throws Exception {
        post(member, ai + "/assistant/messages", "{\"question\":\"%s\"}".formatted("가".repeat(201))).fails(400, "COMMON_INVALID_INPUT");
        post(member, ai + "/tone-checks", "{\"text\":\"%s\",\"target\":\"IDEA_CARD\"}".formatted("가".repeat(2001))).fails(400, "COMMON_INVALID_INPUT");
    }

    @Test
    @DisplayName("요약 목록은 이 방의 요약만 보여 준다")
    void summaryList() throws Exception {
        Long summaryId = insertSummary(owner, "요약 내용");
        Member stranger = newMember("다른방장");
        Long otherRoom = newRoom(stranger);
        jdbc.update("INSERT INTO ai_summary(room_id, scope_type, content, requested_by_account_id) VALUES (?, 'ALL', '남의 요약', ?)",
                otherRoom, stranger.id());

        List<Integer> ids = get(member, ai + "/summaries").ok().read("$.data.content[*].summaryId");

        assertThat(ids).containsExactly(summaryId.intValue());
    }

    @Test
    @DisplayName("요약 수정은 요청자만, 없는 요약은 AI_SUMMARY_NOT_FOUND")
    void summaryUpdate() throws Exception {
        Long summaryId = insertSummary(owner, "초안");

        Response updated = patch(owner, ai + "/summaries/" + summaryId, "{\"content\":\"고친 요약\"}").ok();
        assertThat(updated.<String>read("$.data.content")).isEqualTo("고친 요약");

        Response byOther = patch(member, ai + "/summaries/" + summaryId, "{\"content\":\"남이 고침\"}");
        assertThat(byOther.status()).isIn(403, 404);
        patch(owner, ai + "/summaries/999999999", "{\"content\":\"없음\"}").fails(404, "AI_SUMMARY_NOT_FOUND");
    }

    @Test
    @DisplayName("요약을 카드로 저장하면 진행 중 회차에 카드가 생기고 savedCardId 가 남는다")
    void saveAsCard() throws Exception {
        Long summaryId = insertSummary(owner, "요약 본문");
        String path = ai + "/summaries/" + summaryId + "/save-as-card";

        post(owner, path, "{\"cardType\":\"AGENDA\",\"title\":\"요약 카드\"}").fails(409, "SESSION_NONE_IN_PROGRESS");

        post(owner, "/rooms/" + roomId + "/sessions", "{\"title\":\"회차\"}").ok();
        Long cardId = post(owner, path, "{\"cardType\":\"AGENDA\",\"title\":\"요약 카드\"}").ok().id("$.data.cardId");

        Long saved = jdbc.queryForObject("SELECT saved_card_id FROM ai_summary WHERE id = ?", Long.class, summaryId);
        assertThat(saved).isEqualTo(cardId);
    }

    @Test
    @DisplayName("1000자를 넘는 요약은 카드로 저장하지 않는다(서버가 자르지 않음)")
    void saveAsCardTooLong() throws Exception {
        Long summaryId = insertSummary(owner, "가".repeat(1001));
        post(owner, "/rooms/" + roomId + "/sessions", "{\"title\":\"회차\"}").ok();

        post(owner, ai + "/summaries/" + summaryId + "/save-as-card", "{\"cardType\":\"AGENDA\",\"title\":\"긴 요약\"}")
                .fails(400, "COMMON_INVALID_INPUT");
    }

    @Test
    @DisplayName("톤 점검: AI 서버가 꺼져 있으면 needsChange=false 로 원문 그대로 쓰게 한다")
    void toneCheckFallback() throws Exception {
        Response res = post(member, ai + "/tone-checks", """
                {"text":"이거 왜 이렇게 했어요?","target":"IDEA_CARD"}""").ok();

        assertThat(res.<Boolean>read("$.data.needsChange")).isFalse();
    }

    @Test
    @DisplayName("톤 점검: 내 설정이 OFF 면 AI 를 부르지 않는다")
    void toneCheckOff() throws Exception {
        jdbc.update("UPDATE account SET tone_mode = 'OFF' WHERE id = ?", member.id());

        Response res = post(member, ai + "/tone-checks", """
                {"text":"이거 왜 이렇게 했어요?","target":"IDEA_CARD"}""").ok();

        assertThat(res.<Boolean>read("$.data.needsChange")).isFalse();
        Integer calls = jdbc.queryForObject("SELECT count(*) FROM ai_call_log WHERE room_id = ? AND feature = 'TONE'", Integer.class, roomId);
        assertThat(calls).isZero();
    }

    @Test
    @DisplayName("톤 점검 대상은 IDEA_CARD / CHAT_MESSAGE 만 받는다")
    void toneCheckTarget() throws Exception {
        post(member, ai + "/tone-checks", """
                {"text":"안녕하세요","target":"CARD"}""").fails(400, "COMMON_INVALID_INPUT");
    }

    @Test
    @DisplayName("어시스턴트: AI 서버가 꺼져 있으면 AI_UNAVAILABLE 과 실패 기록, 대화 이력은 본인 것만")
    void assistant() throws Exception {
        post(member, ai + "/assistant/messages", "{\"question\":\"이번 회차 요약해줘\"}").fails(503, "AI_UNAVAILABLE");

        Integer failed = jdbc.queryForObject("SELECT count(*) FROM ai_call_log WHERE room_id = ? AND feature = 'ASSISTANT' AND status = 'FAILED'",
                Integer.class, roomId);
        assertThat(failed).isEqualTo(1);

        List<Integer> askers = get(owner, ai + "/assistant/messages").ok().read("$.data..asker.accountId");
        assertThat(askers).doesNotContain(member.id().intValue());
    }
}
