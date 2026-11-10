package com.fiveoutoffive.moa_backend.briefing;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("11. Briefing")
class BriefingApiTest extends Be2ApiTestSupport {

    Member owner;
    Member member;
    Long roomId;
    String briefing;
    String cards;

    @BeforeEach
    void setUp() throws Exception {
        owner = newMember("방장");
        member = newMember("멤버");
        roomId = newRoom(owner, member);
        briefing = "/rooms/" + roomId + "/briefing";
        cards = "/rooms/" + roomId + "/idea-cards";
        post(member, briefing + "/check").ok();
        post(owner, "/rooms/" + roomId + "/sessions", "{\"title\":\"회차\"}").ok();
    }

    private Long card(Member writer, String title) throws Exception {
        return post(writer, cards, """
                {"cardType":"KEEP","title":"%s"}""".formatted(title)).ok().id("$.data.cardId");
    }

    @Test
    @DisplayName("한 번도 확인하지 않은 첫 참여자는 visible=false")
    void firstVisit() throws Exception {
        Member newbie = newMember("새멤버");
        jdbc.update("INSERT INTO room_member(room_id, account_id, role, status, nickname) VALUES (?, ?, 'MEMBER', 'ACTIVE', ?)",
                roomId, newbie.id(), newbie.name());

        assertThat(get(newbie, briefing).ok().<Boolean>read("$.data.visible")).isFalse();
    }

    @Test
    @DisplayName("남이 만든 카드는 팀 소식에 묶여 나오고 내가 만든 카드는 빠진다")
    void teamNewsExcludesMine() throws Exception {
        card(owner, "방장 카드1");
        card(owner, "방장 카드2");
        card(member, "내 카드");

        Response res = get(member, briefing).ok();

        assertThat(res.<Boolean>read("$.data.visible")).isTrue();
        List<Integer> count = res.read("$.data.teamNews[?(@.eventType=='CARD_CREATED')].count");
        assertThat(count).containsExactly(2);
        List<String> titles = res.read("$.data.teamNews[?(@.eventType=='CARD_CREATED')].items[*].title");
        assertThat(titles).containsExactlyInAnyOrder("방장 카드1", "방장 카드2");
    }

    @Test
    @DisplayName("내 카드에 달린 남의 댓글은 하이라이트에 나온다")
    void commentOnMyCard() throws Exception {
        Long myCard = card(member, "내 카드");
        post(owner, cards + "/" + myCard + "/comments", "{\"content\":\"방장 댓글\"}").ok();

        Response res = get(member, briefing).ok();

        List<Integer> targets = res.read("$.data.highlights.commentsOnMyCards[*].targetId");
        assertThat(targets).containsExactly(myCard.intValue());
    }

    @Test
    @DisplayName("묶음을 소거하면 사라지고, 그 뒤 같은 종류의 새 이벤트는 다시 나온다")
    void dismiss() throws Exception {
        card(owner, "소거 전 카드");

        Response dismissed = post(member, briefing + "/dismiss", "{\"eventType\":\"CARD_CREATED\"}").ok();
        assertThat(dismissed.<List<Object>>read("$.data.teamNews[?(@.eventType=='CARD_CREATED')]")).isEmpty();

        card(owner, "소거 후 카드");
        List<String> titles = get(member, briefing).ok().read("$.data.teamNews[?(@.eventType=='CARD_CREATED')].items[*].title");
        assertThat(titles).containsExactly("소거 후 카드");
    }

    @Test
    @DisplayName("모두 확인하면 팀 소식이 비고, 다른 멤버에게는 영향이 없다")
    void checkAll() throws Exception {
        card(member, "멤버 카드");
        post(owner, briefing + "/check").ok();
        card(owner, "방장 카드");

        Response checked = post(member, briefing + "/check").ok();

        assertThat(checked.<List<Object>>read("$.data.teamNews")).isEmpty();
        Long checkedAt = jdbc.queryForObject("SELECT count(*) FROM room_member WHERE room_id = ? AND account_id = ? AND last_briefing_checked_at IS NOT NULL",
                Long.class, roomId, member.id());
        assertThat(checkedAt).isEqualTo(1L);
        assertThat(get(owner, briefing).ok().<List<Object>>read("$.data.teamNews")).isEmpty();
    }
}
