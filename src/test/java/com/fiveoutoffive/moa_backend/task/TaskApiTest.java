package com.fiveoutoffive.moa_backend.task;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import java.util.List;
import org.junit.jupiter.api.Test;

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

    private static String move(String status, int toIndex, long version) {
        return "{\"status\":\"" + status + "\",\"toIndex\":" + toIndex + ",\"version\":" + version + "}";
    }

    private static long movedVersion(Response moved, Long taskId) {
        List<Number> versions = moved.read("$.data[?(@.taskId == " + taskId + ")].version");
        return versions.getFirst().longValue();
    }
}
