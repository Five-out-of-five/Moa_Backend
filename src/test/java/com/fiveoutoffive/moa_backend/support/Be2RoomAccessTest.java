package com.fiveoutoffive.moa_backend.support;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** BE-2 API 는 모두 방 멤버 전용이다(API 명세 '권한' 칸). */
@DisplayName("BE-2 방 권한")
class Be2RoomAccessTest extends Be2ApiTestSupport {

    @ParameterizedTest(name = "GET {0}")
    @ValueSource(strings = {
            "/dashboard", "/moods", "/sessions/current", "/sessions", "/idea-cards",
            "/briefing", "/conflicts", "/ai/quota", "/ai/summaries", "/ai/assistant/messages"
    })
    @DisplayName("방 멤버가 아니면 ROOM_NOT_MEMBER")
    void notMember(String path) throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        Member stranger = newMember("외부인");

        get(stranger, "/rooms/" + roomId + path).fails(403, "ROOM_NOT_MEMBER");
    }

    @Test
    @DisplayName("토큰이 없으면 401")
    void noToken() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);

        Response res = get(null, "/rooms/" + roomId + "/dashboard");
        if (res.status() != 401) {
            throw new AssertionError("401 을 기대했지만 " + res.status() + " " + res.body());
        }
    }

    @Test
    @DisplayName("나간 멤버는 더 이상 볼 수 없다")
    void leftMember() throws Exception {
        Member owner = newMember("방장");
        Member member = newMember("멤버");
        Long roomId = newRoom(owner, member);
        jdbc.update("UPDATE room_member SET status = 'LEFT', left_at = now() WHERE room_id = ? AND account_id = ?", roomId, member.id());

        get(member, "/rooms/" + roomId + "/idea-cards").fails(403, "ROOM_NOT_MEMBER");
    }
}
