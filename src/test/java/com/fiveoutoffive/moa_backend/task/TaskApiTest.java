package com.fiveoutoffive.moa_backend.task;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.HttpMethod;
import java.nio.charset.StandardCharsets;

/** 업무 API. 낙관적 락(version)이 응답에 저장 후 값으로 나가는지 확인한다. */
class TaskApiTest extends Be2ApiTestSupport {

    @Test
    void 이동_응답의_version_으로_다시_이동할_수_있다() throws Exception {
        Member owner = newMember("업무방장");
        String tasks = "/rooms/" + newRoom(owner) + "/tasks";
        Long taskId = post(owner, tasks, "{\"title\":\"첫 업무\",\"assigneeAccountId\":" + owner.id() + "}").ok().id("$.data.taskId");

        long v0 = get(owner, tasks + "/" + taskId).ok().id("$.data.version");
        long v1 = movedVersion(patch(owner, tasks + "/" + taskId + "/status", move("IN_PROGRESS", 0, v0)).ok(), taskId);

        assertThat(v1).isGreaterThan(v0);
        assertThat(get(owner, tasks + "/" + taskId).ok().id("$.data.version")).isEqualTo(v1);
        patch(owner, tasks + "/" + taskId + "/status", move("REVIEW", 0, v1)).ok();
    }

    @Test
    void 지난_version_으로_이동하면_409() throws Exception {
        Member owner = newMember("업무방장");
        String tasks = "/rooms/" + newRoom(owner) + "/tasks";
        Long taskId = post(owner, tasks, "{\"title\":\"업무\",\"assigneeAccountId\":" + owner.id() + "}").ok().id("$.data.taskId");
        long v0 = get(owner, tasks + "/" + taskId).ok().id("$.data.version");
        patch(owner, tasks + "/" + taskId + "/status", move("IN_PROGRESS", 0, v0)).ok();

        patch(owner, tasks + "/" + taskId + "/status", move("DONE", 0, v0)).fails(409, "TASK_VERSION_CONFLICT");
    }

    @Test
    void 순서만_밀린_업무는_version_이_그대로라_다른_멤버가_바로_옮길_수_있다() throws Exception {
        Member owner = newMember("업무방장");
        String tasks = "/rooms/" + newRoom(owner) + "/tasks";
        Long a = post(owner, tasks, "{\"title\":\"A\",\"assigneeAccountId\":" + owner.id() + "}").ok().id("$.data.taskId");
        Long b = post(owner, tasks, "{\"title\":\"B\",\"assigneeAccountId\":" + owner.id() + "}").ok().id("$.data.taskId");
        long aVersion = get(owner, tasks + "/" + a).ok().id("$.data.version");
        long bVersion = get(owner, tasks + "/" + b).ok().id("$.data.version");

        // B 를 TODO 칸 맨 앞으로 옮기면 A 는 순서만 1칸 밀린다.
        Response moved = patch(owner, tasks + "/" + b + "/status", move("TODO", 0, bVersion)).ok();

        assertThat(movedVersion(moved, a)).isEqualTo(aVersion);
        assertThat(moved.<List<Number>>read("$.data[?(@.taskId == " + a + ")].sortOrder").getFirst().intValue()).isEqualTo(1);
        patch(owner, tasks + "/" + a + "/status", move("IN_PROGRESS", 0, aVersion)).ok();
    }

    @Test
    void 칸반_위치는_같은_스프린트_안에서_센다() throws Exception {
        Member owner = newMember("업무방장");
        Long roomId = newRoom(owner);
        String tasks = "/rooms/" + roomId + "/tasks";
        // 스프린트가 없을 때 만든 업무는 백로그다.
        Long backlog = post(owner, tasks, "{\"title\":\"백로그\",\"assigneeAccountId\":" + owner.id() + "}").ok().id("$.data.taskId");
        post(owner, "/rooms/" + roomId + "/sprints", "{\"name\":\"1차\",\"startDate\":\"2026-11-01\",\"endDate\":\"2026-12-31\"}").ok();
        Long a = post(owner, tasks, "{\"title\":\"A\",\"assigneeAccountId\":" + owner.id() + "}").ok().id("$.data.taskId");
        Long b = post(owner, tasks, "{\"title\":\"B\",\"assigneeAccountId\":" + owner.id() + "}").ok().id("$.data.taskId");

        Response moved = patch(owner, tasks + "/" + b + "/status", move("TODO", 0, get(owner, tasks + "/" + b).ok().id("$.data.version"))).ok();

        List<Number> ids = moved.read("$.data[*].taskId");
        assertThat(ids.stream().map(Number::longValue).toList()).containsExactly(b, a).doesNotContain(backlog);
        assertThat(get(owner, tasks + "/" + backlog).ok().<Integer>read("$.data.sortOrder")).isZero();
    }

    @Test
    void 업무를_삭제하면_첨부도_삭제된다() throws Exception {
        Member owner = newMember("업무방장");
        Long roomId = newRoom(owner);
        String tasks = "/rooms/" + roomId + "/tasks";
        Long fileId = upload(owner, roomId, "TASK_ATTACHMENT");
        Long taskId = post(owner, tasks, "{\"title\":\"첨부 업무\",\"assigneeAccountId\":" + owner.id() + ",\"fileIds\":[" + fileId + "]}").ok().id("$.data.taskId");

        delete(owner, tasks + "/" + taskId).ok();

        assertThat(jdbc.queryForObject("SELECT deleted_at IS NOT NULL FROM file WHERE id = ?", Boolean.class, fileId)).isTrue();
    }

    @Test
    void 등록자_본인이_담당한_업무는_방장이_지워도_알림이_가지_않는다() throws Exception {
        Member owner = newMember("업무방장");
        Member member = newMember("본인담당");
        Long roomId = newRoom(owner, member);
        String tasks = "/rooms/" + roomId + "/tasks";
        Long taskId = post(member, tasks, "{\"title\":\"내 업무\",\"assigneeAccountId\":" + member.id() + "}").ok().id("$.data.taskId");

        delete(owner, tasks + "/" + taskId).ok();

        assertThat(get(member, "/notifications/unread-count").ok().<Integer>read("$.data.count")).isZero();
    }

    @Test
    void 첨부가_5개를_넘으면_명세_코드로_거절한다() throws Exception {
        Member owner = newMember("업무방장");
        String tasks = "/rooms/" + newRoom(owner) + "/tasks";

        post(owner, tasks, "{\"title\":\"t\",\"assigneeAccountId\":" + owner.id() + ",\"fileIds\":[1,2,3,4,5,6]}")
                .fails(409, "FILE_ATTACHMENT_LIMIT_EXCEEDED");
    }

    private Long upload(Member who, Long roomId, String purpose) throws Exception {
        var req = MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/v1/rooms/" + roomId + "/files")
                .file(new MockMultipartFile("file", "memo.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)))
                .param("purpose", purpose)
                .header("Authorization", "Bearer " + who.token());
        var res = mockMvc.perform(req).andReturn().getResponse();
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return new Response(res.getStatus(), res.getContentAsString()).ok().id("$.data.fileId");
    }

    private static String move(String status, int toIndex, long version) {
        return "{\"status\":\"" + status + "\",\"toIndex\":" + toIndex + ",\"version\":" + version + "}";
    }

    private static long movedVersion(Response moved, Long taskId) {
        List<Number> versions = moved.read("$.data[?(@.taskId == " + taskId + ")].version");
        return versions.getFirst().longValue();
    }
}
