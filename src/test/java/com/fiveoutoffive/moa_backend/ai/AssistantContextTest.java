package com.fiveoutoffive.moa_backend.ai;

import com.fiveoutoffive.moa_backend.global.port.out.AiServerPort;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 어시스턴트가 Python 에 넘기는 방 데이터. 작성자·담당자는 방 표시 이름만, 담당자가 없으면 "담당자 없음",
 * 나간 멤버는 "나간 멤버" 로 보낸다. 오늘 날짜와 업무 마감일(한국 날짜)을 함께 보낸다.
 */
@DisplayName("15. AI (어시스턴트 근거 데이터)")
class AssistantContextTest extends Be2ApiTestSupport {

    @MockitoBean
    AiServerPort aiServer;

    @Test
    @DisplayName("작성자·담당자 표시 이름, 담당자 없음·나간 멤버, 마감일과 오늘 날짜를 보낸다")
    @SuppressWarnings("unchecked")
    void sendsPeopleAndDates() throws Exception {
        Member owner = newMember("방장");
        Member member = newMember("멤버");
        Member leaver = newMember("떠난이");
        Long roomId = newRoom(owner, member, leaver);
        String room = "/rooms/" + roomId;
        when(aiServer.isAvailable()).thenReturn(true);
        when(aiServer.stream(eq("/v1/assistant/stream"), any(), any())).thenAnswer(call -> {
            ((Consumer<String>) call.getArgument(2)).accept("{\"delta\":\"답\"}");
            return true;
        });

        post(owner, room + "/sessions", "{\"title\":\"회차\"}").ok();
        post(owner, room + "/idea-cards", "{\"cardType\":\"PROBLEM\",\"title\":\"방장 카드\"}").ok();
        post(leaver, room + "/idea-cards", "{\"cardType\":\"TRY\",\"title\":\"떠난이 카드\"}").ok();
        post(owner, room + "/tasks", "{\"title\":\"멤버 업무\",\"assigneeAccountId\":" + member.id() + ",\"dueDate\":\"2027-01-31\"}").ok();
        Long unassigned = post(owner, room + "/tasks", "{\"title\":\"빈 업무\",\"assigneeAccountId\":" + owner.id() + "}").ok().id("$.data.taskId");
        jdbc.update("UPDATE task SET assignee_account_id = NULL WHERE id = ?", unassigned);
        jdbc.update("UPDATE room_member SET status = 'LEFT' WHERE room_id = ? AND account_id = ?", roomId, leaver.id());

        post(owner, room + "/ai/assistant/messages", "{\"question\":\"누가 뭐 맡았어?\"}");

        ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
        verify(aiServer, timeout(5000)).stream(eq("/v1/assistant/stream"), body.capture(), any());
        Map<String, Object> context = (Map<String, Object>) ((Map<String, Object>) body.getValue()).get("context");

        assertThat(context.get("today")).isEqualTo(KstTime.todayKst().toString());
        List<Map<String, Object>> cards = (List<Map<String, Object>>) context.get("cards");
        assertThat(cards).extracting(c -> c.get("title") + ":" + c.get("writer"))
                .containsExactlyInAnyOrder("방장 카드:방장", "떠난이 카드:나간 멤버");
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) context.get("tasks");
        assertThat(tasks).extracting(t -> t.get("title") + ":" + t.get("assignee") + ":" + t.get("dueDate"))
                .containsExactlyInAnyOrder("멤버 업무:멤버:2027-01-31", "빈 업무:담당자 없음:null");
    }
}
