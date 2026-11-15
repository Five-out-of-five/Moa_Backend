package com.fiveoutoffive.moa_backend.session;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("07. Session")
class SessionApiTest extends Be2ApiTestSupport {

    @Test
    @DisplayName("진행 중 회차가 없으면 current 는 data=null")
    void noCurrent() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);

        Response res = get(owner, "/rooms/" + roomId + "/sessions/current").ok();

        assertThat(res.<Object>read("$.data")).isNull();
    }

    @Test
    @DisplayName("회차 시작 → 진행 중 조회 → 중복 시작은 거절")
    void startAndDuplicate() throws Exception {
        Member owner = newMember("방장");
        Member member = newMember("멤버");
        Long roomId = newRoom(owner, member);
        String path = "/rooms/" + roomId + "/sessions";

        Response started = post(member, path, """
                {"title":"1회차"}""").ok();
        assertThat(started.<Integer>read("$.data.sessionNo")).isEqualTo(1);
        assertThat(started.<String>read("$.data.status")).isEqualTo("IN_PROGRESS");

        Response current = get(owner, path + "/current").ok();
        assertThat(current.id("$.data.sessionId")).isEqualTo(started.id("$.data.sessionId"));

        post(owner, path, """
                {"title":"2회차"}""").fails(409, "SESSION_ALREADY_IN_PROGRESS");
    }

    @Test
    @DisplayName("마감 시각이 과거면 시작하지 않는다")
    void deadlineInPast() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        String past = OffsetDateTime.now(ZoneOffset.ofHours(9)).minusHours(1).toString();

        post(owner, "/rooms/" + roomId + "/sessions", """
                {"title":"지난 회차","deadlineAt":"%s"}""".formatted(past)).fails(400, "SESSION_DEADLINE_IN_PAST");
    }

    @Test
    @DisplayName("마감하면 지난 회차 목록에 들어가고, 다시 마감할 수 없고, 다음 회차 번호는 2")
    void closeFlow() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        String path = "/rooms/" + roomId + "/sessions";
        Long sessionId = post(owner, path, """
                {"title":"1회차"}""").ok().id("$.data.sessionId");
        post(owner, "/rooms/" + roomId + "/idea-cards", """
                {"cardType":"KEEP","title":"카드"}""").ok();

        Response closed = post(owner, path + "/" + sessionId + "/close").ok();
        assertThat(closed.<String>read("$.data.status")).isEqualTo("CLOSED");
        assertThat(closed.<Object>read("$.data.closedAt")).isNotNull();

        post(owner, path + "/" + sessionId + "/close").fails(409, "SESSION_ALREADY_CLOSED");
        assertThat(get(owner, path + "/current").ok().<Object>read("$.data")).isNull();

        Response history = get(owner, path).ok();
        List<Integer> ids = history.read("$.data.content[*].sessionId");
        assertThat(ids).containsExactly(sessionId.intValue());
        assertThat(history.<Integer>read("$.data.content[0].cardCount")).isEqualTo(1);

        Response next = post(owner, path, """
                {"title":"2회차"}""").ok();
        assertThat(next.<Integer>read("$.data.sessionNo")).isEqualTo(2);
    }

    @Test
    @DisplayName("지난 회차 목록은 최신순이고 진행 중 회차는 빠진다")
    void historyOrder() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        String path = "/rooms/" + roomId + "/sessions";
        Long first = post(owner, path, "{\"title\":\"1회차\"}").ok().id("$.data.sessionId");
        post(owner, path + "/" + first + "/close").ok();
        Long second = post(owner, path, "{\"title\":\"2회차\"}").ok().id("$.data.sessionId");
        post(owner, path + "/" + second + "/close").ok();
        post(owner, path, "{\"title\":\"3회차\"}").ok();

        List<Integer> ids = get(owner, path).ok().read("$.data.content[*].sessionId");

        assertThat(ids).containsExactly(second.intValue(), first.intValue());
    }

    @Test
    @DisplayName("없는 회차, 다른 방의 회차는 SESSION_NOT_FOUND")
    void notFound() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        Member other = newMember("다른방장");
        Long otherRoom = newRoom(other);
        Long otherSession = post(other, "/rooms/" + otherRoom + "/sessions", "{\"title\":\"남의 회차\"}").ok().id("$.data.sessionId");

        get(owner, "/rooms/" + roomId + "/sessions/999999999").fails(404, "SESSION_NOT_FOUND");
        get(owner, "/rooms/" + roomId + "/sessions/" + otherSession).fails(404, "SESSION_NOT_FOUND");
        post(owner, "/rooms/" + roomId + "/sessions/" + otherSession + "/close").fails(404, "SESSION_NOT_FOUND");
    }

    /** 두 요청을 최대한 같은 순간에 보내고 상태 코드를 모은다. */
    private List<Integer> concurrently(java.util.concurrent.Callable<Response> a, java.util.concurrent.Callable<Response> b) throws Exception {
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(2);
        java.util.concurrent.CountDownLatch go = new java.util.concurrent.CountDownLatch(1);
        try {
            List<java.util.concurrent.Future<Response>> futures = List.of(
                    pool.submit(() -> { go.await(); return a.call(); }),
                    pool.submit(() -> { go.await(); return b.call(); }));
            go.countDown();
            List<Integer> statuses = new java.util.ArrayList<>();
            for (java.util.concurrent.Future<Response> f : futures) {
                statuses.add(f.get(20, java.util.concurrent.TimeUnit.SECONDS).status());
            }
            return statuses;
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    @DisplayName("두 멤버가 동시에 회차를 시작하면 하나만 성공하고 나머지는 500 이 아니라 409")
    void concurrentStart() throws Exception {
        Member owner = newMember("방장");
        Member member = newMember("멤버");
        Long roomId = newRoom(owner, member);
        String path = "/rooms/" + roomId + "/sessions";

        List<Integer> statuses = concurrently(() -> post(owner, path, "{\"title\":\"A\"}"), () -> post(member, path, "{\"title\":\"B\"}"));

        assertThat(statuses).containsExactlyInAnyOrder(200, 409);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM board_session WHERE room_id = ? AND status = 'IN_PROGRESS'", Integer.class, roomId)).isEqualTo(1);
    }

    @Test
    @DisplayName("두 멤버가 동시에 마감하면 한 번만 마감되고(늦은 쪽 409), 마감 후속 처리도 한 번만 한다")
    void concurrentClose() throws Exception {
        Member owner = newMember("방장");
        Member member = newMember("멤버");
        Long roomId = newRoom(owner, member);
        String path = "/rooms/" + roomId + "/sessions";
        Long sessionId = post(owner, path, "{\"title\":\"회차\"}").ok().id("$.data.sessionId");

        List<Integer> statuses = concurrently(() -> post(owner, path + "/" + sessionId + "/close"), () -> post(member, path + "/" + sessionId + "/close"));

        assertThat(statuses).containsExactlyInAnyOrder(200, 409);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM board_event WHERE room_id = ? AND event_type = 'SESSION_CLOSED' AND target_id = ?", Integer.class, roomId, sessionId)).isEqualTo(1);
    }
}
