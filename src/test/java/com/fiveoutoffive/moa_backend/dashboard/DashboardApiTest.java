package com.fiveoutoffive.moa_backend.dashboard;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("05. Dashboard")
class DashboardApiTest extends Be2ApiTestSupport {

    @Test
    @DisplayName("방을 만들면 기본 위젯 4개가 배치된다")
    void defaultLayout() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);

        Response res = get(owner, "/rooms/" + roomId + "/dashboard").ok();

        List<String> types = res.read("$.data.widgets[*].widgetType");
        assertThat(types).containsExactlyInAnyOrder("MOOD", "IDEA_BOARD", "CATCHUP_BRIEFING", "TASK_BOARD");
    }

    @Test
    @DisplayName("위젯 추가 → 배치 저장 → 제거, 할 때마다 layoutVersion 이 오른다")
    void addMoveRemove() throws Exception {
        Member owner = newMember("방장");
        Member member = newMember("멤버");
        Long roomId = newRoom(owner, member);
        String base = "/rooms/" + roomId + "/dashboard";
        int v0 = get(owner, base).ok().<Integer>read("$.data.layoutVersion");

        Response added = post(member, base + "/widgets", """
                {"widgetType":"MEDIATION","x":0,"y":12,"w":3,"h":3}""").ok();
        Long widgetId = added.first("$.data.widgets[?(@.widgetType=='MEDIATION')].widgetId");
        assertThat(added.<Integer>read("$.data.layoutVersion")).isGreaterThan(v0);

        Response moved = put(owner, base + "/layout", """
                {"widgets":[{"widgetId":%d,"x":6,"y":12,"w":3,"h":3}]}""".formatted(widgetId)).ok();
        List<Integer> xs = moved.read("$.data.widgets[?(@.widgetId==" + widgetId + ")].x");
        assertThat(xs).containsExactly(6);

        Response removed = delete(owner, base + "/widgets/" + widgetId).ok();
        List<String> types = removed.read("$.data.widgets[*].widgetType");
        assertThat(types).doesNotContain("MEDIATION");
    }

    @Test
    @DisplayName("같은 종류 위젯은 방에 1개만 둘 수 있다")
    void duplicateWidget() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);

        post(owner, "/rooms/" + roomId + "/dashboard/widgets", """
                {"widgetType":"MOOD","x":0,"y":12,"w":3,"h":3}""")
                .fails(409, "DASHBOARD_WIDGET_ALREADY_EXISTS");
    }

    @Test
    @DisplayName("최소 크기보다 작거나, 그리드를 벗어나거나, 겹치면 저장하지 않는다")
    void placementRules() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        String widgets = "/rooms/" + roomId + "/dashboard/widgets";

        post(owner, widgets, """
                {"widgetType":"MEDIATION","x":0,"y":12,"w":2,"h":2}""").fails(400, "DASHBOARD_WIDGET_TOO_SMALL");
        post(owner, widgets, """
                {"widgetType":"MEDIATION","x":11,"y":12,"w":3,"h":3}""").fails(400, "DASHBOARD_WIDGET_OUT_OF_GRID");
        post(owner, widgets, """
                {"widgetType":"MEDIATION","x":0,"y":0,"w":3,"h":3}""").fails(409, "DASHBOARD_WIDGET_OVERLAP");
    }

    @Test
    @DisplayName("마지막 위젯은 제거할 수 없다")
    void lastWidget() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        String base = "/rooms/" + roomId + "/dashboard";
        List<Integer> ids = get(owner, base).ok().read("$.data.widgets[*].widgetId");

        for (int i = 0; i < ids.size() - 1; i++) {
            delete(owner, base + "/widgets/" + ids.get(i)).ok();
        }
        delete(owner, base + "/widgets/" + ids.get(ids.size() - 1)).fails(409, "DASHBOARD_LAST_WIDGET");
    }

    @Test
    @DisplayName("없는 위젯을 제거하면 DASHBOARD_WIDGET_NOT_FOUND")
    void removeUnknownWidget() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);

        delete(owner, "/rooms/" + roomId + "/dashboard/widgets/999999999").fails(404, "DASHBOARD_WIDGET_NOT_FOUND");
    }

    @Test
    @DisplayName("초기화하면 기본 4개 구성으로 돌아간다")
    void reset() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        String base = "/rooms/" + roomId + "/dashboard";
        post(owner, base + "/widgets", """
                {"widgetType":"MEDIATION","x":0,"y":12,"w":3,"h":3}""").ok();

        Response res = post(owner, base + "/reset").ok();

        List<String> types = res.read("$.data.widgets[*].widgetType");
        assertThat(types).containsExactlyInAnyOrder("MOOD", "IDEA_BOARD", "CATCHUP_BRIEFING", "TASK_BOARD");
    }

    @Test
    @DisplayName("AI 서버가 꺼져 있으면 구성 추천은 AI_UNAVAILABLE, 기존 배치는 그대로")
    void aiRecommendationUnavailable() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        String base = "/rooms/" + roomId + "/dashboard";
        String before = get(owner, base).ok().body();

        post(owner, base + "/ai-recommendations", """
                {"purpose":"주간 회고"}""").fails(503, "AI_UNAVAILABLE");

        assertThat(get(owner, base).ok().body()).isEqualTo(before);
    }

    @Test
    @DisplayName("AI 구성 적용 후 되돌리면 이전 배치로 돌아가고, 두 번은 되돌릴 수 없다")
    void applyAndUndo() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        String base = "/rooms/" + roomId + "/dashboard";
        List<String> before = get(owner, base).ok().read("$.data.widgets[*].widgetType");

        post(owner, base + "/ai-recommendations/apply", """
                {"widgets":[
                  {"widgetType":"IDEA_BOARD","x":0,"y":0,"w":6,"h":5},
                  {"widgetType":"MEDIATION","x":6,"y":0,"w":3,"h":3}
                ]}""").ok();
        List<String> applied = get(owner, base).ok().read("$.data.widgets[*].widgetType");
        assertThat(applied).containsExactlyInAnyOrder("IDEA_BOARD", "MEDIATION");

        post(owner, base + "/ai-recommendations/undo").ok();
        List<String> undone = get(owner, base).ok().read("$.data.widgets[*].widgetType");
        assertThat(undone).containsExactlyInAnyOrderElementsOf(before);

        post(owner, base + "/ai-recommendations/undo").fails(409, "DASHBOARD_UNDO_NOT_AVAILABLE");
    }

    @Test
    @DisplayName("배치 저장: 없는 위젯·다른 방 위젯, 최소 크기 미만, 그리드 밖(높이 8 초과), 겹침")
    void layoutErrors() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        String base = "/rooms/" + roomId + "/dashboard";
        Response current = get(owner, base).ok();
        Long mood = current.first("$.data.widgets[?(@.widgetType=='MOOD')].widgetId");
        Long ideaBoard = current.first("$.data.widgets[?(@.widgetType=='IDEA_BOARD')].widgetId");
        Member other = newMember("다른방장");
        Long otherWidget = get(other, "/rooms/" + newRoom(other) + "/dashboard").ok().id("$.data.widgets[0].widgetId");

        put(owner, base + "/layout", "{\"widgets\":[{\"widgetId\":999999999,\"x\":0,\"y\":0,\"w\":3,\"h\":3}]}")
                .fails(404, "DASHBOARD_WIDGET_NOT_FOUND");
        put(owner, base + "/layout", "{\"widgets\":[{\"widgetId\":%d,\"x\":0,\"y\":20,\"w\":3,\"h\":3}]}".formatted(otherWidget))
                .fails(404, "DASHBOARD_WIDGET_NOT_FOUND");
        put(owner, base + "/layout", "{\"widgets\":[{\"widgetId\":%d,\"x\":3,\"y\":0,\"w\":5,\"h\":5}]}".formatted(ideaBoard))
                .fails(400, "DASHBOARD_WIDGET_TOO_SMALL");
        put(owner, base + "/layout", "{\"widgets\":[{\"widgetId\":%d,\"x\":0,\"y\":20,\"w\":3,\"h\":9}]}".formatted(mood))
                .fails(400, "DASHBOARD_WIDGET_OUT_OF_GRID");
        put(owner, base + "/layout", "{\"widgets\":[{\"widgetId\":%d,\"x\":4,\"y\":0,\"w\":3,\"h\":4}]}".formatted(mood))
                .fails(409, "DASHBOARD_WIDGET_OVERLAP");
        put(owner, base + "/layout", "{\"widgets\":[]}").fails(400, "COMMON_INVALID_INPUT");
    }

    @Test
    @DisplayName("AI 구성 적용: 크기·그리드·겹침 검사, 종류 중복·종류 누락은 400 (500 이 아니어야 함)")
    void applyErrors() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        String apply = "/rooms/" + roomId + "/dashboard/ai-recommendations/apply";

        post(owner, apply, "{\"widgets\":[{\"widgetType\":\"IDEA_BOARD\",\"x\":0,\"y\":0,\"w\":5,\"h\":5}]}")
                .fails(400, "DASHBOARD_WIDGET_TOO_SMALL");
        post(owner, apply, "{\"widgets\":[{\"widgetType\":\"MOOD\",\"x\":10,\"y\":0,\"w\":3,\"h\":3}]}")
                .fails(400, "DASHBOARD_WIDGET_OUT_OF_GRID");
        post(owner, apply, """
                {"widgets":[{"widgetType":"MOOD","x":0,"y":0,"w":3,"h":3},{"widgetType":"VOTE","x":2,"y":2,"w":3,"h":3}]}""")
                .fails(409, "DASHBOARD_WIDGET_OVERLAP");
        post(owner, apply, """
                {"widgets":[{"widgetType":"MOOD","x":0,"y":0,"w":3,"h":3},{"widgetType":"MOOD","x":3,"y":0,"w":3,"h":3}]}""")
                .fails(400, "COMMON_INVALID_INPUT");
        post(owner, apply, "{\"widgets\":[{\"x\":0,\"y\":0,\"w\":3,\"h\":3}]}").fails(400, "COMMON_INVALID_INPUT");

        List<String> types = get(owner, "/rooms/" + roomId + "/dashboard").ok().read("$.data.widgets[*].widgetType");
        assertThat(types).containsExactlyInAnyOrder("MOOD", "IDEA_BOARD", "CATCHUP_BRIEFING", "TASK_BOARD");
    }

    @Test
    @DisplayName("구성 추천: 오늘 한도(LAYOUT 5회)를 다 쓰면 AI_QUOTA_EXCEEDED, 목적이 비면 400")
    void recommendationQuota() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        String path = "/rooms/" + roomId + "/dashboard/ai-recommendations";
        jdbc.update("INSERT INTO ai_usage_quota(room_id, feature, quota_date, used_count) VALUES (?, 'LAYOUT', (now() AT TIME ZONE 'Asia/Seoul')::date, 5)", roomId);

        post(owner, path, "{\"purpose\":\"\"}").fails(400, "COMMON_INVALID_INPUT");
        post(owner, path, "{\"purpose\":\"주간 회고\"}").fails(429, "AI_QUOTA_EXCEEDED");
    }

    @Test
    @DisplayName("적용 후 누군가 배치를 바꾸면 되돌릴 수 없다")
    void undoAfterLayoutChange() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        String base = "/rooms/" + roomId + "/dashboard";
        Response applied = post(owner, base + "/ai-recommendations/apply", """
                {"widgets":[{"widgetType":"IDEA_BOARD","x":0,"y":0,"w":6,"h":5}]}""").ok();
        Long ideaBoard = applied.id("$.data.widgets[0].widgetId");

        put(owner, base + "/layout", """
                {"widgets":[{"widgetId":%d,"x":0,"y":0,"w":7,"h":5}]}""".formatted(ideaBoard)).ok();

        post(owner, base + "/ai-recommendations/undo").fails(409, "DASHBOARD_UNDO_NOT_AVAILABLE");
    }
}
