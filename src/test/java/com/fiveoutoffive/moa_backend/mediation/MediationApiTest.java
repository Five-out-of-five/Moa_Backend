package com.fiveoutoffive.moa_backend.mediation;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 충돌 감지는 AI 서버가 해야 하므로, 감지 결과(card_conflict)와 절충안 초안(compromise_draft)은 DB 에 직접 넣고 시험한다.
 */
@DisplayName("12. Mediation")
class MediationApiTest extends Be2ApiTestSupport {

    Member owner;
    Member member;
    Long roomId;
    String conflicts;
    Long sessionId;
    Long cardA;
    Long cardB;

    @BeforeEach
    void setUp() throws Exception {
        owner = newMember("방장");
        member = newMember("멤버");
        roomId = newRoom(owner, member);
        conflicts = "/rooms/" + roomId + "/conflicts";
        sessionId = post(owner, "/rooms/" + roomId + "/sessions", "{\"title\":\"회차\"}").ok().id("$.data.sessionId");
        cardA = post(owner, "/rooms/" + roomId + "/idea-cards", """
                {"cardType":"AGENDA","title":"주 1회 회의"}""").ok().id("$.data.cardId");
        cardB = post(member, "/rooms/" + roomId + "/idea-cards", """
                {"cardType":"AGENDA","title":"회의 없애기"}""").ok().id("$.data.cardId");
    }

    private void addMediationWidget() throws Exception {
        post(owner, "/rooms/" + roomId + "/dashboard/widgets", """
                {"widgetType":"MEDIATION","x":0,"y":12,"w":3,"h":3}""").ok();
    }

    private Long insertConflict() {
        return jdbc.queryForObject("""
                INSERT INTO card_conflict(room_id, card_a_id, card_b_id, reason) VALUES (?, ?, ?, '회의 빈도가 반대') RETURNING id
                """, Long.class, roomId, Math.min(cardA, cardB), Math.max(cardA, cardB));
    }

    private Long insertDraft(Long conflictId, Member requester) {
        jdbc.update("UPDATE card_conflict SET status = 'PROPOSED' WHERE id = ?", conflictId);
        return jdbc.queryForObject("""
                INSERT INTO compromise_draft(conflict_id, requested_by_account_id, card_type, title, content)
                VALUES (?, ?, 'AGENDA', '격주 회의', '두 의견 절충') RETURNING id
                """, Long.class, conflictId, requester.id());
    }

    @Test
    @DisplayName("중재 위젯이 없으면 MEDIATION_WIDGET_REQUIRED")
    void widgetRequired() throws Exception {
        get(owner, conflicts).fails(409, "MEDIATION_WIDGET_REQUIRED");
    }

    @Test
    @DisplayName("충돌 목록과 상세(두 카드 비교)")
    void listAndDetail() throws Exception {
        addMediationWidget();
        Long conflictId = insertConflict();

        List<Integer> ids = get(member, conflicts).ok().read("$.data[*].conflictId");
        assertThat(ids).containsExactly(conflictId.intValue());

        Response detail = get(member, conflicts + "/" + conflictId).ok();
        assertThat(detail.<String>read("$.data.status")).isEqualTo("DETECTED");
        assertThat(List.of(detail.id("$.data.cardA.cardId"), detail.id("$.data.cardB.cardId"))).containsExactlyInAnyOrder(cardA, cardB);
    }

    @Test
    @DisplayName("누구나 해제할 수 있고 두 번은 해제할 수 없다")
    void dismiss() throws Exception {
        addMediationWidget();
        Long conflictId = insertConflict();

        post(member, conflicts + "/" + conflictId + "/dismiss").ok();

        assertThat(get(owner, conflicts + "/" + conflictId).ok().<String>read("$.data.status")).isEqualTo("DISMISSED");
        post(owner, conflicts + "/" + conflictId + "/dismiss").fails(409, "CONFLICT_ALREADY_DISMISSED");
    }

    @Test
    @DisplayName("해제한 충돌에는 절충안을 만들 수 없고 상태도 그대로다")
    void compromiseOnDismissed() throws Exception {
        addMediationWidget();
        Long conflictId = insertConflict();
        post(member, conflicts + "/" + conflictId + "/dismiss").ok();

        post(owner, conflicts + "/" + conflictId + "/compromise-drafts").fails(409, "CONFLICT_ALREADY_DISMISSED");

        assertThat(get(owner, conflicts + "/" + conflictId).ok().<String>read("$.data.status")).isEqualTo("DISMISSED");
    }

    @Test
    @DisplayName("없는 충돌은 CONFLICT_NOT_FOUND")
    void notFound() throws Exception {
        addMediationWidget();

        get(owner, conflicts + "/999999999").fails(404, "CONFLICT_NOT_FOUND");
        post(owner, conflicts + "/999999999/dismiss").fails(404, "CONFLICT_NOT_FOUND");
    }

    @Test
    @DisplayName("충돌 카드 하나가 삭제되면 충돌도 목록에서 빠진다")
    void deletedCardRemovesConflict() throws Exception {
        addMediationWidget();
        insertConflict();

        delete(owner, "/rooms/" + roomId + "/idea-cards/" + cardA).ok();

        assertThat(get(owner, conflicts).ok().<List<Object>>read("$.data")).isEmpty();
    }

    @Test
    @DisplayName("AI 서버가 꺼져 있으면 절충안 생성은 AI_UNAVAILABLE")
    void compromiseUnavailable() throws Exception {
        addMediationWidget();
        Long conflictId = insertConflict();

        post(owner, conflicts + "/" + conflictId + "/compromise-drafts").fails(503, "AI_UNAVAILABLE");
    }

    @Test
    @DisplayName("초안은 요청자에게만 보이고, 채택하면 진행 중 회차에 AI 카드가 생긴다")
    void adopt() throws Exception {
        addMediationWidget();
        Long conflictId = insertConflict();
        Long draftId = insertDraft(conflictId, owner);
        String draftPath = conflicts + "/" + conflictId + "/compromise-drafts/" + draftId;

        assertThat(get(owner, conflicts + "/" + conflictId).ok().id("$.data.myDraft.draftId")).isEqualTo(draftId);
        assertThat(get(member, conflicts + "/" + conflictId).ok().<Object>read("$.data.myDraft")).isNull();
        post(member, draftPath + "/adopt", "{\"title\":\"남이 채택\"}").fails(404, "DRAFT_NOT_FOUND");

        Response adopted = post(owner, draftPath + "/adopt", """
                {"title":"격주 회의","content":"수정한 절충안"}""").ok();
        Long newCard = adopted.id("$.data.cardId");

        Response card = get(member, "/rooms/" + roomId + "/idea-cards/" + newCard).ok();
        assertThat(card.<Boolean>read("$.data.isAiGenerated")).isTrue();
        assertThat(card.id("$.data.sessionId")).isEqualTo(sessionId);
        assertThat(get(owner, conflicts + "/" + conflictId).ok().id("$.data.adoptedCardId")).isEqualTo(newCard);

        post(owner, draftPath + "/adopt", "{\"title\":\"한 번 더\"}").fails(409, "CONFLICT_ALREADY_ADOPTED");
    }

    @Test
    @DisplayName("폐기한 초안은 채택할 수 없다")
    void adoptDiscarded() throws Exception {
        addMediationWidget();
        Long conflictId = insertConflict();
        Long draftId = insertDraft(conflictId, owner);
        String draftPath = conflicts + "/" + conflictId + "/compromise-drafts/" + draftId;
        post(owner, draftPath + "/discard").ok();

        post(owner, draftPath + "/adopt", "{\"title\":\"격주 회의\"}").fails(409, "DRAFT_ALREADY_PROCESSED");
    }

    @Test
    @DisplayName("상세·절충안 요청도 중재 위젯이 없으면 MEDIATION_WIDGET_REQUIRED")
    void widgetRequiredEverywhere() throws Exception {
        Long conflictId = insertConflict();

        get(owner, conflicts + "/" + conflictId).fails(409, "MEDIATION_WIDGET_REQUIRED");
        post(owner, conflicts + "/" + conflictId + "/compromise-drafts").fails(409, "MEDIATION_WIDGET_REQUIRED");
    }

    @Test
    @DisplayName("절충안 요청: 없는 충돌, 이미 채택된 충돌, 오늘 한도(COMPROMISE 10회) 초과")
    void compromiseErrors() throws Exception {
        addMediationWidget();
        Long conflictId = insertConflict();

        post(owner, conflicts + "/999999999/compromise-drafts").fails(404, "CONFLICT_NOT_FOUND");

        jdbc.update("INSERT INTO ai_usage_quota(room_id, feature, quota_date, used_count) VALUES (?, 'COMPROMISE', (now() AT TIME ZONE 'Asia/Seoul')::date, 10)", roomId);
        post(owner, conflicts + "/" + conflictId + "/compromise-drafts").fails(429, "AI_QUOTA_EXCEEDED");

        jdbc.update("UPDATE card_conflict SET adopted_card_id = ? WHERE id = ?", cardA, conflictId);
        post(owner, conflicts + "/" + conflictId + "/compromise-drafts").fails(409, "CONFLICT_ALREADY_ADOPTED");
    }

    @Test
    @DisplayName("다른 방의 충돌은 CONFLICT_NOT_FOUND")
    void otherRoomConflict() throws Exception {
        addMediationWidget();
        Long conflictId = insertConflict();
        Member stranger = newMember("다른방장");
        Long otherRoom = newRoom(stranger);
        post(stranger, "/rooms/" + otherRoom + "/dashboard/widgets", """
                {"widgetType":"MEDIATION","x":0,"y":12,"w":3,"h":3}""").ok();

        get(stranger, "/rooms/" + otherRoom + "/conflicts/" + conflictId).fails(404, "CONFLICT_NOT_FOUND");
        post(stranger, "/rooms/" + otherRoom + "/conflicts/" + conflictId + "/dismiss").fails(404, "CONFLICT_NOT_FOUND");
    }

    @Test
    @DisplayName("남의 초안은 폐기할 수 없다(DRAFT_NOT_FOUND)")
    void discardOthersDraft() throws Exception {
        addMediationWidget();
        Long conflictId = insertConflict();
        Long draftId = insertDraft(conflictId, owner);

        post(member, conflicts + "/" + conflictId + "/compromise-drafts/" + draftId + "/discard").fails(404, "DRAFT_NOT_FOUND");
        post(owner, conflicts + "/" + conflictId + "/compromise-drafts/999999999/discard").fails(404, "DRAFT_NOT_FOUND");
    }

    @Test
    @DisplayName("진행 중 회차가 없으면 채택할 수 없다")
    void adoptWithoutSession() throws Exception {
        addMediationWidget();
        Long conflictId = insertConflict();
        Long draftId = insertDraft(conflictId, owner);
        post(owner, "/rooms/" + roomId + "/sessions/" + sessionId + "/close").ok();

        post(owner, conflicts + "/" + conflictId + "/compromise-drafts/" + draftId + "/adopt", "{\"title\":\"격주 회의\"}")
                .fails(409, "SESSION_NONE_IN_PROGRESS");
    }

    @Test
    @DisplayName("폐기하면 충돌이 REDETECTED 로 바뀌고, 폐기한 초안은 다시 처리할 수 없다")
    void discard() throws Exception {
        addMediationWidget();
        Long conflictId = insertConflict();
        Long draftId = insertDraft(conflictId, owner);
        String draftPath = conflicts + "/" + conflictId + "/compromise-drafts/" + draftId;

        post(owner, draftPath + "/discard").ok();

        assertThat(get(owner, conflicts + "/" + conflictId).ok().<String>read("$.data.status")).isEqualTo("REDETECTED");
        post(owner, draftPath + "/discard").fails(409, "DRAFT_ALREADY_PROCESSED");
    }
}
