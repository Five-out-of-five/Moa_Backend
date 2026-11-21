package com.fiveoutoffive.moa_backend.mood;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("06. Mood")
class MoodApiTest extends Be2ApiTestSupport {

    @Test
    @DisplayName("같은 날 다시 등록하면 새 행이 아니라 갱신된다")
    void upsertSameDay() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        String path = "/rooms/" + roomId + "/moods";

        put(owner, path + "/me", """
                {"mood":"GOOD","comment":"좋아요"}""").ok();
        put(owner, path + "/me", """
                {"mood":"TIRED","comment":"피곤"}""").ok();

        Response team = get(owner, path).ok();
        List<String> moods = team.read("$.data.members[?(@.accountId==" + owner.id() + ")].mood");
        assertThat(moods).containsExactly("TIRED");
        assertThat(team.<Integer>read("$.data.distribution.TIRED")).isEqualTo(1);
        assertThat(team.<Integer>read("$.data.distribution.GOOD")).isZero();
        Integer rows = jdbc.queryForObject("SELECT count(*) FROM mood_status WHERE room_id = ? AND account_id = ?",
                Integer.class, roomId, owner.id());
        assertThat(rows).isEqualTo(1);
    }

    @Test
    @DisplayName("아직 등록하지 않은 멤버는 mood=null 로 내려온다")
    void unregisteredMember() throws Exception {
        Member owner = newMember("방장");
        Member member = newMember("멤버");
        Long roomId = newRoom(owner, member);
        put(owner, "/rooms/" + roomId + "/moods/me", """
                {"mood":"VERY_GOOD"}""").ok();

        Response team = get(member, "/rooms/" + roomId + "/moods").ok();

        List<Object> memberMood = team.read("$.data.members[?(@.accountId==" + member.id() + ")].mood");
        assertThat(memberMood).hasSize(1).containsNull();
    }

    @Test
    @DisplayName("오늘 기분을 남기고 나간 멤버는 분포·오늘 추이에서도 빠진다 (목록과 숫자가 맞는다)")
    void leftMemberNotCounted() throws Exception {
        Member owner = newMember("방장");
        Member member = newMember("멤버");
        Long roomId = newRoom(owner, member);
        String path = "/rooms/" + roomId + "/moods";
        put(owner, path + "/me", """
                {"mood":"GOOD"}""").ok();
        put(member, path + "/me", """
                {"mood":"TIRED"}""").ok();
        jdbc.update("UPDATE room_member SET status = 'LEFT' WHERE room_id = ? AND account_id = ?", roomId, member.id());

        Response team = get(owner, path + "?period=DAYS_7").ok();

        assertThat(team.<List<Object>>read("$.data.members")).hasSize(1);
        assertThat(team.<Integer>read("$.data.distribution.GOOD")).isEqualTo(1);
        assertThat(team.<Integer>read("$.data.distribution.TIRED")).isZero();
        assertThat(team.<Integer>read("$.data.trend[6].counts.TIRED")).isZero();
        assertThat(team.<List<Object>>read("$.data.trend")).hasSize(7);
    }

    @Test
    @DisplayName("기간 값이 명세(TODAY/DAYS_7/DAYS_30)가 아니면 400")
    void invalidPeriod() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);

        get(owner, "/rooms/" + roomId + "/moods?period=7").fails(400, "COMMON_INVALID_INPUT");
        assertThat(get(owner, "/rooms/" + roomId + "/moods?period=DAYS_30").ok().<List<Object>>read("$.data.trend")).hasSize(30);
    }

    @Test
    @DisplayName("아무도 등록하지 않으면 분포가 모두 0")
    void emptyDistribution() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);

        Response team = get(owner, "/rooms/" + roomId + "/moods").ok();

        java.util.Map<String, Integer> distribution = team.read("$.data.distribution");
        assertThat(distribution.values()).allMatch(count -> count == 0);
    }

    @Test
    @DisplayName("코멘트는 50자까지")
    void commentLength() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);

        put(owner, "/rooms/" + roomId + "/moods/me", """
                {"mood":"GOOD","comment":"%s"}""".formatted("가".repeat(51))).fails(400, "COMMON_INVALID_INPUT");
    }

    @Test
    @DisplayName("오늘 첫 기분을 동시에 두 번 보내도 둘 다 성공하고 한 건만 남는다")
    void firstMoodTwiceTogether() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        String path = "/rooms/" + roomId + "/moods/me";

        List<Integer> statuses = sendTogether(() -> put(owner, path, "{\"mood\":\"GOOD\",\"comment\":\"a\"}"), () -> put(owner, path, "{\"mood\":\"TIRED\",\"comment\":\"b\"}"));

        assertThat(statuses).containsExactly(200, 200);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM mood_status WHERE room_id = ? AND account_id = ?", Integer.class, roomId, owner.id())).isEqualTo(1);
    }
}
