package com.fiveoutoffive.moa_backend.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** 알림·알림 설정 API. 나간 방 알림 제외와 관리자·시스템 그룹 저장 규칙을 확인한다. */
class NotificationApiTest extends Be2ApiTestSupport {

    @Test
    void 방을_나가면_그_방의_알림이_목록과_안읽은_수에서_빠진다() throws Exception {
        Member owner = newMember("방장");
        Member member = newMember("멤버");
        Long roomId = newRoom(owner, member);
        createVote(owner, roomId);
        assertThat(unreadCount(member)).isEqualTo(1);

        delete(member, "/rooms/" + roomId + "/members/me").ok();

        assertThat(unreadCount(member)).isZero();
        assertThat(get(member, "/notifications").ok().<Integer>read("$.data.totalElements")).isZero();
    }

    @Test
    void 나간_멤버에게는_그_방의_새_알림을_만들지_않는다() throws Exception {
        Member owner = newMember("방장");
        Member member = newMember("등록자");
        Long roomId = newRoom(owner, member);
        String tasks = "/rooms/" + roomId + "/tasks";
        // 멤버가 등록하고 방장에게 맡긴 업무. 상태가 바뀌면 등록자에게 알림이 간다.
        Long taskId = post(member, tasks, "{\"title\":\"넘긴 업무\",\"assigneeAccountId\":" + owner.id() + "}").ok().id("$.data.taskId");
        delete(member, "/rooms/" + roomId + "/members/me").ok();
        long version = get(owner, tasks + "/" + taskId).ok().id("$.data.version");

        patch(owner, tasks + "/" + taskId + "/status", "{\"status\":\"DONE\",\"toIndex\":0,\"version\":" + version + "}").ok();

        assertThat(unreadCount(member)).isZero();
    }

    @Test
    void 내보내진_멤버는_퇴출_알림만_받는다() throws Exception {
        Member owner = newMember("방장");
        Member member = newMember("퇴출대상");
        Long roomId = newRoom(owner, member);
        createVote(owner, roomId);

        delete(owner, "/rooms/" + roomId + "/members/" + member.id()).ok();

        List<String> types = get(member, "/notifications").ok().read("$.data.content[*].type");
        assertThat(types).containsExactly("MEMBER_KICKED");
    }

    @Test
    void 조회한_전역_설정을_그대로_저장할_수_있다() throws Exception {
        Member me = newMember("설정");
        String fetched = get(me, "/notification-settings").ok().body();
        // 조회 결과에는 SYSTEM(enabled=true, locked=true) 이 들어 있다. locked 는 요청 필드가 아니라 무시된다.
        String body = "{\"groups\":" + JsonMapper.builder().build().readTree(fetched).at("/data/groups") + "}";

        Response saved = put(me, "/notification-settings", body).ok();

        assertThat(saved.<List<Boolean>>read("$.data.groups[?(@.group == 'SYSTEM')].enabled")).containsExactly(true);
    }

    @Test
    void 관리자_시스템_그룹을_끄면_400() throws Exception {
        Member me = newMember("설정");

        put(me, "/notification-settings", "{\"groups\":[{\"group\":\"SYSTEM\",\"enabled\":false}]}").fails(400, "NOTIFICATION_GROUP_LOCKED");
    }

    @Test
    void 방별_설정은_SYSTEM_을_비워_보내도_저장된다() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);

        Response saved = put(owner, "/rooms/" + roomId + "/notification-settings",
                "{\"muted\":false,\"groups\":[{\"group\":\"TASK\",\"enabled\":false},{\"group\":\"SYSTEM\",\"enabled\":null}]}").ok();

        assertThat(saved.<List<Boolean>>read("$.data.groups[?(@.group == 'TASK')].effective")).containsExactly(false);
        assertThat(saved.<List<Boolean>>read("$.data.groups[?(@.group == 'SYSTEM')].effective")).containsExactly(true);
    }

    private void createVote(Member who, Long roomId) throws Exception {
        String deadline = OffsetDateTime.now(ZoneOffset.ofHours(9)).plusHours(3).withNano(0).toString();
        post(who, "/rooms/" + roomId + "/votes", "{\"title\":\"투표\",\"voteType\":\"YES_NO\",\"deadlineAt\":\"" + deadline + "\",\"anonymous\":false}").ok();
    }

    private int unreadCount(Member who) throws Exception {
        return get(who, "/notifications/unread-count").ok().<Integer>read("$.data.count");
    }
}
