package com.fiveoutoffive.moa_backend.vote;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
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

    @Test
    void 수정하는_도중_누가_참여하면_수정이_반영되지_않는다() throws Exception {
        Member owner = newMember("투표방장");
        Member member = newMember("참여자");
        String votes = "/rooms/" + newRoom(owner, member) + "/votes";
        Response created = post(owner, votes, vote("SINGLE", "[\"A\",\"B\"]", null)).ok();
        Long voteId = created.id("$.data.voteId");
        long v0 = created.id("$.data.version");
        List<Number> optionIds = created.read("$.data.options[*].optionId");

        put(member, votes + "/" + voteId + "/ballots/me", "{\"optionIds\":[" + optionIds.getFirst() + "]}").ok();

        patch(owner, votes + "/" + voteId, "{\"title\":\"바뀐 제목\",\"version\":" + v0 + "}").fails(409, "VOTE_VERSION_CONFLICT");
        long fresh = get(owner, votes + "/" + voteId).ok().id("$.data.version");
        patch(owner, votes + "/" + voteId, "{\"title\":\"바뀐 제목\",\"version\":" + fresh + "}").fails(409, "VOTE_HAS_PARTICIPANTS");
    }

    @Test
    void 동시에_참여해도_서로_막지_않는다() throws Exception {
        Member owner = newMember("투표방장");
        Member a = newMember("참여A");
        Member b = newMember("참여B");
        String votes = "/rooms/" + newRoom(owner, a, b) + "/votes";
        Response created = post(owner, votes, vote("SINGLE", "[\"A\",\"B\"]", null)).ok();
        Long voteId = created.id("$.data.voteId");
        List<Number> optionIds = created.read("$.data.options[*].optionId");

        put(a, votes + "/" + voteId + "/ballots/me", "{\"optionIds\":[" + optionIds.getFirst() + "]}").ok();
        put(b, votes + "/" + voteId + "/ballots/me", "{\"optionIds\":[" + optionIds.getLast() + "]}").ok();
        put(owner, votes + "/" + voteId + "/ballots/me", "{\"optionIds\":[" + optionIds.getFirst() + "]}").ok();

        assertThat(get(owner, votes + "/" + voteId).ok().<String>read("$.data.status")).isEqualTo("CLOSED");
    }

    @Test
    void 수정_후_최대_선택_개수가_규칙에_맞지_않으면_거절한다() throws Exception {
        Member owner = newMember("투표방장");
        String votes = "/rooms/" + newRoom(owner) + "/votes";
        Response single = post(owner, votes, vote("SINGLE", "[\"A\",\"B\"]", null)).ok();
        Response multiple = post(owner, votes, vote("MULTIPLE", "[\"A\",\"B\",\"C\"]", 3)).ok();

        patch(owner, votes + "/" + single.id("$.data.voteId"), "{\"maxSelectCount\":2,\"version\":" + single.id("$.data.version") + "}")
                .fails(400, "VOTE_MAX_SELECT_INVALID");
        patch(owner, votes + "/" + multiple.id("$.data.voteId"), "{\"options\":[\"A\",\"B\"],\"version\":" + multiple.id("$.data.version") + "}")
                .fails(400, "VOTE_MAX_SELECT_INVALID");
    }

    @Test
    void 입력값_오류는_명세_에러_코드로_돌려준다() throws Exception {
        Member owner = newMember("투표방장");
        String votes = "/rooms/" + newRoom(owner) + "/votes";

        post(owner, votes, vote("SINGLE", "[\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\",\"11\"]", null))
                .fails(400, "VOTE_OPTION_COUNT_INVALID");
        post(owner, votes, vote("MULTIPLE", "[\"A\",\"B\"]", 0)).fails(400, "VOTE_MAX_SELECT_INVALID");
        Long voteId = post(owner, votes, vote("SINGLE", "[\"A\",\"B\"]", null)).ok().id("$.data.voteId");
        put(owner, votes + "/" + voteId + "/ballots/me", "{\"optionIds\":[]}").fails(400, "VOTE_SELECTION_INVALID");
    }

    private static String vote(String type, String options, Integer maxSelect) {
        String deadline = OffsetDateTime.now(ZoneOffset.ofHours(9)).plusHours(3).withNano(0).toString();
        return "{\"title\":\"투표\",\"voteType\":\"" + type + "\",\"options\":" + options
                + (maxSelect == null ? "" : ",\"maxSelectCount\":" + maxSelect)
                + ",\"deadlineAt\":\"" + deadline + "\",\"anonymous\":false}";
    }
}
