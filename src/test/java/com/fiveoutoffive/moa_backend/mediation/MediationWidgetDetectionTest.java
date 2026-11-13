package com.fiveoutoffive.moa_backend.mediation;

import com.fiveoutoffive.moa_backend.ai.application.result.AiEmbeddingPayload;
import com.fiveoutoffive.moa_backend.global.port.out.AiServerPort;
import com.fiveoutoffive.moa_backend.mediation.application.result.AiConflictPayload;
import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 중재 위젯을 새로 추가하면 진행 중 회차의 기존 카드도 충돌 분석한다. 마감된 회차 카드는 분석하지 않는다.
 * AI 서버는 가짜로 바꿔, 후보 중 첫 카드와 충돌한다고 답하게 한다.
 */
@DisplayName("12. Mediation (중재 위젯 추가 시 감지)")
class MediationWidgetDetectionTest extends Be2ApiTestSupport {

    private static final String DETECT = "/v1/conflicts/detect";

    @MockitoBean
    AiServerPort aiServer;

    Member owner;
    Long roomId;

    @BeforeEach
    void setUp() {
        owner = newMember("방장");
        roomId = newRoom(owner);
        List<Double> vector = Collections.nCopies(768, 0.1);
        when(aiServer.isAvailable()).thenReturn(true);
        when(aiServer.post(eq("/v1/embeddings"), any(), eq(AiEmbeddingPayload.class)))
                .thenReturn(Optional.of(new AiEmbeddingPayload(vector, "test-embedding")));
        when(aiServer.post(eq(DETECT), any(), eq(AiConflictPayload.class))).thenAnswer(call -> {
            Map<?, ?> body = call.getArgument(1);
            List<?> candidates = (List<?>) body.get("candidates");
            Long other = ((Number) ((Map<?, ?>) candidates.getFirst()).get("id")).longValue();
            return Optional.of(new AiConflictPayload(List.of(new AiConflictPayload.Item(other, "서로 반대"))));
        });
    }

    private Long startSession() throws Exception {
        return post(owner, "/rooms/" + roomId + "/sessions", "{\"title\":\"회차\"}").ok().id("$.data.sessionId");
    }

    private Long card(String title) throws Exception {
        return post(owner, "/rooms/" + roomId + "/idea-cards", "{\"cardType\":\"AGENDA\",\"title\":\"" + title + "\"}").ok().id("$.data.cardId");
    }

    private void addMediationWidget() throws Exception {
        post(owner, "/rooms/" + roomId + "/dashboard/widgets", """
                {"widgetType":"MEDIATION","x":0,"y":12,"w":3,"h":3}""").ok();
    }

    private int conflictCount() {
        return jdbc.queryForObject("SELECT count(*) FROM card_conflict WHERE room_id = ?", Integer.class, roomId);
    }

    /** 분석은 커밋 후 비동기로 돈다. 결과가 생길 때까지 잠깐 기다린다. */
    private int awaitConflicts() throws InterruptedException {
        for (int i = 0; i < 50 && conflictCount() == 0; i++) {
            Thread.sleep(100);
        }
        return conflictCount();
    }

    /** 카드 등록 후 임베딩 저장(비동기)이 끝날 때까지 기다린다. */
    private void awaitEmbeddings(int expected) throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            Integer count = jdbc.queryForObject("SELECT count(*) FROM content_embedding WHERE room_id = ?", Integer.class, roomId);
            if (count != null && count >= expected) {
                return;
            }
            Thread.sleep(100);
        }
    }

    @Test
    @DisplayName("위젯을 넣기 전에 올라온 진행 중 회차 카드끼리도, 위젯을 추가하면 바로 분석한다")
    void detectsExistingCardsWhenWidgetAdded() throws Exception {
        startSession();
        Long a = card("주 1회 회의");
        Long b = card("회의 없애기");
        awaitEmbeddings(2);
        verify(aiServer, never()).post(eq(DETECT), any(), any());

        addMediationWidget();

        assertThat(awaitConflicts()).isEqualTo(1);
        Map<String, Object> row = jdbc.queryForMap("SELECT card_a_id, card_b_id FROM card_conflict WHERE room_id = ?", roomId);
        assertThat(List.of(((Number) row.get("card_a_id")).longValue(), ((Number) row.get("card_b_id")).longValue()))
                .containsExactly(Math.min(a, b), Math.max(a, b));
    }

    @Test
    @DisplayName("마감된 회차의 카드는 위젯을 추가해도 분석하지 않는다")
    void skipsClosedSession() throws Exception {
        Long closed = startSession();
        card("주 1회 회의");
        card("회의 없애기");
        awaitEmbeddings(2);
        post(owner, "/rooms/" + roomId + "/sessions/" + closed + "/close").ok();
        startSession();

        addMediationWidget();

        verify(aiServer, after(1500).never()).post(eq(DETECT), any(), any());
        assertThat(conflictCount()).isZero();
    }

    @Test
    @DisplayName("AI 구성 적용으로 중재 위젯이 생겨도 분석한다")
    void detectsWhenRecommendationAddsWidget() throws Exception {
        startSession();
        card("주 1회 회의");
        card("회의 없애기");
        awaitEmbeddings(2);

        post(owner, "/rooms/" + roomId + "/dashboard/ai-recommendations/apply", """
                {"widgets":[
                  {"widgetType":"IDEA_BOARD","x":0,"y":0,"w":6,"h":5},
                  {"widgetType":"MEDIATION","x":6,"y":0,"w":3,"h":3}]}""").ok();

        assertThat(awaitConflicts()).isEqualTo(1);
    }

    @Test
    @DisplayName("이미 중재 위젯이 있으면 배치를 다시 적용해도 다시 분석하지 않는다")
    void noRerunWhenAlreadyPresent() throws Exception {
        startSession();
        addMediationWidget();
        verify(aiServer, after(500).never()).post(eq(DETECT), any(), any());

        post(owner, "/rooms/" + roomId + "/dashboard/ai-recommendations/apply", """
                {"widgets":[
                  {"widgetType":"IDEA_BOARD","x":0,"y":0,"w":6,"h":5},
                  {"widgetType":"MEDIATION","x":6,"y":0,"w":3,"h":3}]}""").ok();

        verify(aiServer, after(1000).never()).post(anyString(), any(), eq(AiConflictPayload.class));
    }
}
