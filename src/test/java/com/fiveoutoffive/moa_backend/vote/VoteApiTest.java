package com.fiveoutoffive.moa_backend.vote;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/** 투표 API. 수정 응답의 version 이 저장 후 값인지 확인한다. */
class VoteApiTest extends Be2ApiTestSupport {

    @Test
    void 수정_응답의_version_으로_다시_수정할_수_있다() throws Exception {
        Member owner = newMember("투표방장");
        String votes = "/rooms/" + newRoom(owner) + "/votes";
        String deadline = OffsetDateTime.now(ZoneOffset.ofHours(9)).plusHours(3).withNano(0).toString();
        Response created = post(owner, votes, "{\"title\":\"점심\",\"voteType\":\"SINGLE\",\"options\":[\"국밥\",\"냉면\"],"
                + "\"deadlineAt\":\"" + deadline + "\",\"anonymous\":false}").ok();
        Long voteId = created.id("$.data.voteId");
        long v0 = created.id("$.data.version");

        long v1 = patch(owner, votes + "/" + voteId, "{\"title\":\"저녁\",\"version\":" + v0 + "}").ok().id("$.data.version");

        assertThat(v1).isGreaterThan(v0);
        assertThat(get(owner, votes + "/" + voteId).ok().id("$.data.version")).isEqualTo(v1);
        patch(owner, votes + "/" + voteId, "{\"title\":\"야식\",\"version\":" + v1 + "}").ok();
        patch(owner, votes + "/" + voteId, "{\"title\":\"간식\",\"version\":" + v0 + "}").fails(409, "VOTE_VERSION_CONFLICT");
    }
}
