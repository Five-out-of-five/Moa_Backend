package com.fiveoutoffive.moa_backend.ai;

import com.fiveoutoffive.moa_backend.ai.application.result.AiEmbeddingPayload;
import com.fiveoutoffive.moa_backend.ai.application.result.AiTonePayload;
import com.fiveoutoffive.moa_backend.ai.application.service.AiGateway;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import com.fiveoutoffive.moa_backend.dashboard.application.result.LayoutRecommendResponse;
import com.fiveoutoffive.moa_backend.global.port.out.AiServerPort;
import com.fiveoutoffive.moa_backend.mediation.application.result.AiCompromisePayload;
import com.fiveoutoffive.moa_backend.mediation.application.result.AiConflictPayload;
import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * AI 안정성: 한도 선점·반납, 어시스턴트 전용 실행기, 충돌 감지 결과 검증, 트랜잭션 밖 AI 호출 후 저장.
 * 동시 스트림 거절을 보려고 어시스턴트 동시 스트림 수를 1 로 둔다.
 */
@DisplayName("15. AI (안정성)")
@TestPropertySource(properties = "app.ai.assistant-max-streams=1")
class AiStabilityTest extends Be2ApiTestSupport {

    @MockitoBean
    AiServerPort aiServer;

    @Autowired
    AiGateway gateway;

    Member owner;
    Member member;
    Long roomId;
    String room;

    @BeforeEach
    void setUp() throws Exception {
        owner = newMember("방장");
        member = newMember("멤버");
        roomId = newRoom(owner, member);
        room = "/rooms/" + roomId;
    }

    private void useUp(String feature, int used) {
        jdbc.update("""
                INSERT INTO ai_usage_quota(room_id, feature, quota_date, used_count)
                VALUES (?, ?, (now() AT TIME ZONE 'Asia/Seoul')::date, ?)
                """, roomId, feature, used);
    }

    private int used(String feature) {
        List<Integer> rows = jdbc.queryForList("""
                SELECT used_count FROM ai_usage_quota WHERE room_id = ? AND feature = ? AND quota_date = (now() AT TIME ZONE 'Asia/Seoul')::date
                """, Integer.class, roomId, feature);
        return rows.isEmpty() ? 0 : rows.getFirst();
    }

    private static void waitUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10_000;
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError("10초 안에 조건이 맞지 않음");
            }
            Thread.sleep(50);
        }
    }

    private static AiEmbeddingPayload embedding() {
        return new AiEmbeddingPayload(new ArrayList<>(Collections.nCopies(768, 0.1)), "test-embedding");
    }

    // ---------------------------------------------------------------- 한도 선점

    @Test
    @DisplayName("한도: 1회 남은 방에 동시에 8번 선점해도 1번만 성공하고 한도를 넘지 않는다")
    void reserveIsAtomic() throws Exception {
        useUp("SUMMARY", 9);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        for (int i = 0; i < 8; i++) {
            pool.submit(() -> {
                start.await();
                gateway.tryReserve(roomId, AiFeature.SUMMARY).ifPresent(d -> success.incrementAndGet());
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        assertThat(success.get()).isEqualTo(1);
        assertThat(used("SUMMARY")).isEqualTo(10);
    }

    @Test
    @DisplayName("한도: 한도가 0 인 기능은 선점하지 못하고 사용량 행도 만들지 않는다")
    void zeroLimitNeverReserves() {
        assertThat(gateway.tryReserve(roomId, AiFeature.TASK_RECOMMEND)).isEmpty();
        Integer rows = jdbc.queryForObject("SELECT count(*) FROM ai_usage_quota WHERE room_id = ?", Integer.class, roomId);
        assertThat(rows).isZero();
    }

    @Test
    @DisplayName("톤: AI 가 실패하면 원문 통과(needsChange=false)이고 선점한 1회를 돌려준다. 성공하면 1회 차감")
    void toneReleasesOnFailure() throws Exception {
        String body = """
                {"text":"이거 다시 해와","target":"IDEA_CARD"}""";

        Response failed = post(member, room + "/ai/tone-checks", body).ok();
        assertThat(failed.<Boolean>read("$.data.needsChange")).isFalse();
        assertThat(used("TONE")).isZero();

        when(aiServer.post(eq("/v1/tone-checks"), any(), eq(AiTonePayload.class))).thenReturn(Optional.of(new AiTonePayload(true, "다시 해 줄 수 있어?")));
        Response ok = post(member, room + "/ai/tone-checks", body).ok();
        assertThat(ok.<Boolean>read("$.data.needsChange")).isTrue();
        assertThat(used("TONE")).isEqualTo(1);
    }

    @Test
    @DisplayName("레이아웃 추천: AI 가 실패하면 503 이고 사용량은 그대로다")
    void layoutReleasesOnFailure() throws Exception {
        post(owner, room + "/dashboard/ai-recommendations", "{\"purpose\":\"회고 위주\"}").fails(503, "AI_UNAVAILABLE");
        assertThat(used("LAYOUT")).isZero();

        when(aiServer.post(eq("/v1/layouts/recommend"), any(), eq(LayoutRecommendResponse.class))).thenReturn(Optional.of(new LayoutRecommendResponse(List.of(), "기본 구성")));
        post(owner, room + "/dashboard/ai-recommendations", "{\"purpose\":\"회고 위주\"}").ok();
        assertThat(used("LAYOUT")).isEqualTo(1);
    }

    // ---------------------------------------------------------------- 어시스턴트

    @Test
    @DisplayName("어시스턴트: AI 서버가 끊긴 상태면 503 이고 선점한 1회를 돌려준다")
    void assistantUnavailableReleases() throws Exception {
        when(aiServer.isAvailable()).thenReturn(false);

        post(member, room + "/ai/assistant/messages", "{\"question\":\"질문\"}").fails(503, "AI_UNAVAILABLE");
        assertThat(used("ASSISTANT")).isZero();
    }

    @Test
    @DisplayName("어시스턴트: 답이 하나도 없으면(FAILED) 반납, 끝까지 받으면(COMPLETED) 1회 차감")
    @SuppressWarnings("unchecked")
    void assistantChargesOnlyAnswered() throws Exception {
        when(aiServer.isAvailable()).thenReturn(true);
        when(aiServer.stream(eq("/v1/assistant/stream"), any(), any())).thenReturn(false);

        post(member, room + "/ai/assistant/messages", "{\"question\":\"질문\"}");
        waitUntil(() -> jdbc.queryForObject("SELECT count(*) FROM ai_assistant_message WHERE room_id = ? AND status = 'FAILED'", Integer.class, roomId) == 1);
        assertThat(used("ASSISTANT")).isZero();

        when(aiServer.stream(eq("/v1/assistant/stream"), any(), any())).thenAnswer(call -> {
            ((Consumer<String>) call.getArgument(2)).accept("{\"delta\":\"답\"}");
            return true;
        });
        post(member, room + "/ai/assistant/messages", "{\"question\":\"질문\"}");
        waitUntil(() -> jdbc.queryForObject("SELECT count(*) FROM ai_assistant_message WHERE room_id = ? AND status = 'COMPLETED'", Integer.class, roomId) == 1);
        assertThat(used("ASSISTANT")).isEqualTo(1);
    }

    @Test
    @DisplayName("어시스턴트: 동시 스트림이 꽉 차면 큐에 쌓지 않고 바로 503, 거절된 요청은 한도를 쓰지 않는다")
    void assistantRejectsWhenBusy() throws Exception {
        CountDownLatch streaming = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(1);
        when(aiServer.isAvailable()).thenReturn(true);
        when(aiServer.stream(eq("/v1/assistant/stream"), any(), any())).thenAnswer(call -> {
            streaming.countDown();
            finish.await(10, TimeUnit.SECONDS);
            return false;
        });
        try {
            post(member, room + "/ai/assistant/messages", "{\"question\":\"첫 질문\"}");
            assertThat(streaming.await(5, TimeUnit.SECONDS)).isTrue();

            post(owner, room + "/ai/assistant/messages", "{\"question\":\"두 번째\"}").fails(503, "AI_UNAVAILABLE");
            assertThat(used("ASSISTANT")).isEqualTo(1);
        } finally {
            finish.countDown();
        }
        waitUntil(() -> used("ASSISTANT") == 0);
    }

    // ---------------------------------------------------------------- 충돌 감지

    @Test
    @DisplayName("충돌 감지: AI 가 준 ID 중 후보에 있는 카드만 저장하고, 같은 쌍을 다시 받아도 오류 없이 1건, 임베딩은 남는다")
    void conflictOnlyTrustsCandidates() throws Exception {
        post(owner, room + "/dashboard/widgets", """
                {"widgetType":"MEDIATION","x":0,"y":12,"w":3,"h":3}""").ok();
        Long sessionId = post(owner, room + "/sessions", "{\"title\":\"회차\"}").ok().id("$.data.sessionId");
        Member stranger = newMember("다른방장");
        Long otherRoom = newRoom(stranger);
        post(stranger, "/rooms/" + otherRoom + "/sessions", "{\"title\":\"남의 회차\"}").ok();
        Long foreignCard = post(stranger, "/rooms/" + otherRoom + "/idea-cards", "{\"cardType\":\"TRY\",\"title\":\"남의 카드\"}").ok().id("$.data.cardId");

        doReturn(Optional.of(embedding())).when(aiServer).post(eq("/v1/embeddings"), any(), eq(AiEmbeddingPayload.class));
        Long cardA = post(owner, room + "/idea-cards", "{\"cardType\":\"TRY\",\"title\":\"리뷰를 강화하자\"}").ok().id("$.data.cardId");
        waitUntil(() -> jdbc.queryForObject("SELECT count(*) FROM content_embedding WHERE card_id = ?", Integer.class, cardA) == 1);

        // 새 카드 B 의 후보는 A 하나다. AI 가 A 외에 자기 자신·없는 ID·다른 방 카드를 섞어 줘도 A 만 저장해야 한다.
        AtomicInteger detectCalls = new AtomicInteger();
        Long[] cardB = new Long[1];
        doAnswer(call -> {
            detectCalls.incrementAndGet();
            return Optional.of(new AiConflictPayload(List.of(
                    new AiConflictPayload.Item(cardA, "리뷰 강도가 반대"),
                    new AiConflictPayload.Item(cardB[0], "자기 자신"),
                    new AiConflictPayload.Item(999_999_999L, "없는 카드"),
                    new AiConflictPayload.Item(foreignCard, "다른 방 카드"))));
        }).when(aiServer).post(eq("/v1/conflicts/detect"), any(), eq(AiConflictPayload.class));
        cardB[0] = post(member, room + "/idea-cards", "{\"cardType\":\"TRY\",\"title\":\"리뷰를 줄이자\"}").ok().id("$.data.cardId");
        waitUntil(() -> jdbc.queryForObject("SELECT count(*) FROM card_conflict WHERE room_id = ?", Integer.class, roomId) == 1);

        List<Long> pair = jdbc.queryForList("SELECT card_a_id FROM card_conflict WHERE room_id = ? UNION ALL SELECT card_b_id FROM card_conflict WHERE room_id = ?", Long.class, roomId, roomId);
        assertThat(pair).containsExactlyInAnyOrder(cardA, cardB[0]);
        Integer foreign = jdbc.queryForObject("SELECT count(*) FROM card_conflict WHERE card_a_id = ? OR card_b_id = ?", Integer.class, foreignCard, foreignCard);
        assertThat(foreign).isZero();

        // 회차 마감 때 같은 쌍을 다시 받는다. 예전에는 유니크 위반으로 트랜잭션이 통째로 롤백됐다.
        int before = detectCalls.get();
        post(owner, room + "/sessions/" + sessionId + "/close").ok();
        waitUntil(() -> detectCalls.get() >= before + 2);
        Thread.sleep(300);

        Integer conflicts = jdbc.queryForObject("SELECT count(*) FROM card_conflict WHERE room_id = ?", Integer.class, roomId);
        assertThat(conflicts).isEqualTo(1);
        Integer embeddings = jdbc.queryForObject("SELECT count(*) FROM content_embedding WHERE card_id IN (?, ?)", Integer.class, cardA, cardB[0]);
        assertThat(embeddings).isEqualTo(2);
    }

    // ---------------------------------------------------------------- 중재

    @Test
    @DisplayName("절충안: AI 호출 뒤 짧은 트랜잭션으로 초안과 PROPOSED 상태를 저장하고 1회 차감한다")
    void compromiseSavesAfterCall() throws Exception {
        // 카드 등록 뒤 비동기 임베딩 처리가 같은 목을 부르므로, 스텁은 카드를 만들기 전에 doReturn 으로 건다.
        doReturn(Optional.of(new AiCompromisePayload("격주 회의", "두 의견 절충"))).when(aiServer).post(eq("/v1/compromises"), any(), eq(AiCompromisePayload.class));
        post(owner, room + "/dashboard/widgets", """
                {"widgetType":"MEDIATION","x":0,"y":12,"w":3,"h":3}""").ok();
        post(owner, room + "/sessions", "{\"title\":\"회차\"}").ok();
        Long a = post(owner, room + "/idea-cards", "{\"cardType\":\"AGENDA\",\"title\":\"주 1회 회의\"}").ok().id("$.data.cardId");
        Long b = post(member, room + "/idea-cards", "{\"cardType\":\"AGENDA\",\"title\":\"회의 없애기\"}").ok().id("$.data.cardId");
        Long conflictId = jdbc.queryForObject("INSERT INTO card_conflict(room_id, card_a_id, card_b_id, reason) VALUES (?, ?, ?, '반대') RETURNING id",
                Long.class, roomId, Math.min(a, b), Math.max(a, b));

        Response res = post(member, room + "/conflicts/" + conflictId + "/compromise-drafts").ok();

        assertThat(res.<String>read("$.data.title")).isEqualTo("격주 회의");
        assertThat(jdbc.queryForObject("SELECT status FROM card_conflict WHERE id = ?", String.class, conflictId)).isEqualTo("PROPOSED");
        assertThat(used("COMPROMISE")).isEqualTo(1);
    }

    @Test
    @DisplayName("중재: 충돌 행이 다른 방 카드를 가리키면 그 카드를 보여주지 않고 CARD_NOT_FOUND")
    void conflictDetailHidesForeignCard() throws Exception {
        post(owner, room + "/dashboard/widgets", """
                {"widgetType":"MEDIATION","x":0,"y":12,"w":3,"h":3}""").ok();
        post(owner, room + "/sessions", "{\"title\":\"회차\"}").ok();
        Long mine = post(owner, room + "/idea-cards", "{\"cardType\":\"TRY\",\"title\":\"내 카드\"}").ok().id("$.data.cardId");
        Member stranger = newMember("다른방장");
        Long otherRoom = newRoom(stranger);
        post(stranger, "/rooms/" + otherRoom + "/sessions", "{\"title\":\"남의 회차\"}").ok();
        Long foreign = post(stranger, "/rooms/" + otherRoom + "/idea-cards", "{\"cardType\":\"TRY\",\"title\":\"남의 비밀 카드\"}").ok().id("$.data.cardId");
        Long conflictId = jdbc.queryForObject("INSERT INTO card_conflict(room_id, card_a_id, card_b_id, reason) VALUES (?, ?, ?, '잘못 저장된 쌍') RETURNING id",
                Long.class, roomId, Math.min(mine, foreign), Math.max(mine, foreign));

        Response res = get(owner, room + "/conflicts/" + conflictId);

        assertThat(res.status()).isEqualTo(404);
        assertThat(res.body()).doesNotContain("남의 비밀 카드");
    }
}
