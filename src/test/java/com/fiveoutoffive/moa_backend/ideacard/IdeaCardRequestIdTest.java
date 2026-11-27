package com.fiveoutoffive.moa_backend.ideacard;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 카드 등록 재시도 키(requestId). 응답을 못 받은 프론트가 같은 본문·같은 키로 다시 보내도 카드는 하나만 생기고,
 * 처음 카드를 200 으로 돌려받는다.
 */
@DisplayName("08. IdeaCard (등록 재시도 requestId)")
class IdeaCardRequestIdTest extends Be2ApiTestSupport {

    Member owner;
    Member member;
    Long roomId;
    String cards;
    Long sessionId;

    @BeforeEach
    void setUp() throws Exception {
        owner = newMember("방장");
        member = newMember("멤버");
        roomId = newRoom(owner, member);
        cards = "/rooms/" + roomId + "/idea-cards";
        sessionId = post(owner, "/rooms/" + roomId + "/sessions", "{\"title\":\"회차\"}").ok().id("$.data.sessionId");
    }

    private String body(UUID requestId, String title) {
        return """
                {"cardType":"KEEP","title":"%s","content":"내용","sessionId":%d,"requestId":"%s"}""".formatted(title, sessionId, requestId);
    }

    private int cardsWithKey(UUID requestId) {
        return jdbc.queryForObject("SELECT count(*) FROM idea_card WHERE request_id = ?", Integer.class, requestId);
    }

    private Long uploadedFile(Member uploader) {
        return jdbc.queryForObject("""
                INSERT INTO file(owner_account_id, room_id, file_group, object_key, original_name, mime_type, size_bytes)
                VALUES (?, ?, 'CARD_ATTACHMENT', ?, 'memo.txt', 'text/plain', 10) RETURNING id
                """, Long.class, uploader.id(), roomId, "test/" + UUID.randomUUID());
    }

    @Test
    @DisplayName("같은 키로 다시 보내면 새로 만들지 않고 처음 카드를 200 으로 돌려준다 (이벤트·첨부도 다시 하지 않음)")
    void retryReturnsFirstCard() throws Exception {
        UUID key = UUID.randomUUID();
        Long fileId = uploadedFile(member);
        String request = """
                {"cardType":"AGENDA","title":"첨부 카드","content":"내용","fileIds":[%d],"sessionId":%d,"requestId":"%s"}""".formatted(fileId, sessionId, key);

        Long first = post(member, cards, request).ok().id("$.data.cardId");
        Response retry = post(member, cards, request).ok();

        assertThat(retry.id("$.data.cardId")).isEqualTo(first);
        assertThat(retry.<List<Object>>read("$.data.files")).hasSize(1);
        assertThat(cardsWithKey(key)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM card_file WHERE card_id = ?", Integer.class, first)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM board_event WHERE room_id = ? AND event_type = 'CARD_CREATED' AND target_id = ?", Integer.class, roomId, first)).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 키가 동시에 여러 번 와도 카드는 1장이고 모두 같은 카드를 200 으로 받는다")
    void concurrentRetriesMakeOneCard() throws Exception {
        UUID key = UUID.randomUUID();
        String request = body(key, "동시 저장");
        List<Callable<Response>> calls = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            calls.add(() -> post(member, cards, request));
        }

        List<Response> responses = concurrently(calls);

        assertThat(responses).extracting(Response::status).containsOnly(200);
        assertThat(responses.stream().map(r -> r.id("$.data.cardId")).distinct()).hasSize(1);
        assertThat(cardsWithKey(key)).isEqualTo(1);
    }

    @Test
    @DisplayName("처음 요청이 저장된 뒤 회차가 마감돼도 재시도는 처음 카드를 받는다")
    void retryAfterSessionClosed() throws Exception {
        UUID key = UUID.randomUUID();
        Long first = post(member, cards, body(key, "마감 전 저장")).ok().id("$.data.cardId");
        post(owner, "/rooms/" + roomId + "/sessions/" + sessionId + "/close").ok();

        assertThat(post(member, cards, body(key, "마감 전 저장")).ok().id("$.data.cardId")).isEqualTo(first);
    }

    @Test
    @DisplayName("같은 키에 다른 본문이면 409 CARD_REQUEST_ID_CONFLICT, 카드는 늘지 않는다")
    void differentBodyIsRejected() throws Exception {
        UUID key = UUID.randomUUID();
        post(member, cards, body(key, "처음 제목")).ok();

        post(member, cards, body(key, "바꾼 제목")).fails(409, "CARD_REQUEST_ID_CONFLICT");
        post(member, cards, """
                {"cardType":"TRY","title":"처음 제목","content":"내용","sessionId":%d,"requestId":"%s"}""".formatted(sessionId, key)).fails(409, "CARD_REQUEST_ID_CONFLICT");
        assertThat(cardsWithKey(key)).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 키를 다른 방에 쓰면 다른 요청이라 409")
    void sameKeyInOtherRoomIsRejected() throws Exception {
        UUID key = UUID.randomUUID();
        post(owner, cards, body(key, "카드")).ok();
        Long otherRoom = newRoom(owner);
        Long otherSession = post(owner, "/rooms/" + otherRoom + "/sessions", "{\"title\":\"다른 방 회차\"}").ok().id("$.data.sessionId");

        post(owner, "/rooms/" + otherRoom + "/idea-cards", """
                {"cardType":"KEEP","title":"카드","content":"내용","sessionId":%d,"requestId":"%s"}""".formatted(otherSession, key)).fails(409, "CARD_REQUEST_ID_CONFLICT");
    }

    @Test
    @DisplayName("내용 생략과 빈 문자열은 같은 요청으로 본다")
    void omittedContentEqualsEmpty() throws Exception {
        UUID key = UUID.randomUUID();
        Long first = post(member, cards, """
                {"cardType":"KEEP","title":"내용 없음","sessionId":%d,"requestId":"%s"}""".formatted(sessionId, key)).ok().id("$.data.cardId");

        Long retry = post(member, cards, """
                {"cardType":"KEEP","title":"내용 없음","content":"","fileIds":[],"sessionId":%d,"requestId":"%s"}""".formatted(sessionId, key)).ok().id("$.data.cardId");

        assertThat(retry).isEqualTo(first);
    }

    @Test
    @DisplayName("처음 카드를 그사이 수정했어도 처음과 같은 본문의 재시도는 200 이고 카드의 지금 상태를 돌려준다")
    void retryAfterEditReturnsCurrentCard() throws Exception {
        UUID key = UUID.randomUUID();
        Long first = post(member, cards, body(key, "처음 제목")).ok().id("$.data.cardId");
        patch(member, cards + "/" + first, "{\"title\":\"고친 제목\"}").ok();

        Response retry = post(member, cards, body(key, "처음 제목")).ok();

        assertThat(retry.id("$.data.cardId")).isEqualTo(first);
        assertThat(retry.<String>read("$.data.title")).isEqualTo("고친 제목");
    }

    @Test
    @DisplayName("처음 카드를 지웠으면 되살리지 않고 404 CARD_NOT_FOUND")
    void deletedFirstCardIsNotRecreated() throws Exception {
        UUID key = UUID.randomUUID();
        Long first = post(member, cards, body(key, "지울 카드")).ok().id("$.data.cardId");
        delete(member, cards + "/" + first).ok();

        post(member, cards, body(key, "지울 카드")).fails(404, "CARD_NOT_FOUND");
        assertThat(cardsWithKey(key)).isEqualTo(1);
    }

    @Test
    @DisplayName("키는 작성자별이다: 다른 사람이 같은 키를 써도 각자 카드가 생긴다")
    void keyIsPerWriter() throws Exception {
        UUID key = UUID.randomUUID();
        Long mine = post(member, cards, body(key, "같은 키")).ok().id("$.data.cardId");
        Long theirs = post(owner, cards, body(key, "같은 키")).ok().id("$.data.cardId");

        assertThat(theirs).isNotEqualTo(mine);
        assertThat(cardsWithKey(key)).isEqualTo(2);
    }

    @Test
    @DisplayName("키를 빼면 예전처럼 보낼 때마다 새 카드, UUID 형식이 아니면 COMMON_INVALID_INPUT")
    void withoutKeyAndInvalidKey() throws Exception {
        String request = """
                {"cardType":"KEEP","title":"키 없음","sessionId":%d}""".formatted(sessionId);
        Long a = post(member, cards, request).ok().id("$.data.cardId");
        Long b = post(member, cards, request).ok().id("$.data.cardId");
        assertThat(a).isNotEqualTo(b);

        post(member, cards, """
                {"cardType":"KEEP","title":"잘못된 키","requestId":"not-a-uuid"}""").fails(400, "COMMON_INVALID_INPUT");
    }

    private List<Response> concurrently(List<Callable<Response>> calls) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(calls.size());
        CountDownLatch go = new CountDownLatch(1);
        try {
            List<Future<Response>> futures = new ArrayList<>();
            for (Callable<Response> call : calls) {
                futures.add(pool.submit(() -> {
                    go.await();
                    return call.call();
                }));
            }
            go.countDown();
            List<Response> responses = new ArrayList<>();
            for (Future<Response> f : futures) {
                responses.add(f.get(20, TimeUnit.SECONDS));
            }
            return responses;
        } finally {
            pool.shutdownNow();
        }
    }
}
