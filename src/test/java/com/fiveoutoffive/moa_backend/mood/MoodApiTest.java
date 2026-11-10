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
}
