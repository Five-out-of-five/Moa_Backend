package com.fiveoutoffive.moa_backend.notice;

import com.fiveoutoffive.moa_backend.global.security.GlobalJwtProvider;
import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 18. Notice(사용자 4) + 19. Admin 공지 관리(5). 요구사항 78.
 */
@DisplayName("18. Notice · 19. Admin 공지 관리")
class NoticeApiTest extends Be2ApiTestSupport {

    private static final ZoneOffset KST = ZoneOffset.ofHours(9);

    @Autowired
    private GlobalJwtProvider jwtProvider;

    private Member newAdmin(String name) {
        Member m = newMember(name);
        jdbc.update("UPDATE account SET role = 'ADMIN' WHERE id = ?", m.id());
        return new Member(m.id(), name, jwtProvider.createAccessToken(String.valueOf(m.id()), "ADMIN"));
    }

    private String at(OffsetDateTime t) {
        return t.truncatedTo(ChronoUnit.SECONDS).toString();
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(KST);
    }

    private Long create(Member admin, String type, String title, OffsetDateTime start, OffsetDateTime end, OffsetDateTime maintenance) throws Exception {
        String extra = maintenance == null ? "" : ",\"maintenanceStartsAt\":\"%s\"".formatted(at(maintenance));
        return post(admin, "/admin/notices", """
                {"noticeType":"%s","title":"%s","content":"# 내용","displayStartsAt":"%s","displayEndsAt":"%s"%s}"""
                .formatted(type, title, at(start), at(end), extra)).ok().id("$.data.noticeId");
    }

    @Test
    @DisplayName("일반 회원은 공지를 등록할 수 없다")
    void userCannotCreate() throws Exception {
        post(newMember("회원"), "/admin/notices", """
                {"noticeType":"GENERAL","title":"t","content":"c","displayStartsAt":"%s","displayEndsAt":"%s"}"""
                .formatted(at(now()), at(now().plusDays(1)))).fails(403, "COMMON_FORBIDDEN");
    }

    @Test
    @DisplayName("노출 종료가 시작보다 늦어야 한다")
    void periodInvalid() throws Exception {
        Member admin = newAdmin("관리자");

        post(admin, "/admin/notices", """
                {"noticeType":"GENERAL","title":"t","content":"c","displayStartsAt":"%s","displayEndsAt":"%s"}"""
                .formatted(at(now().plusDays(2)), at(now().plusDays(1)))).fails(400, "ADMIN_NOTICE_PERIOD_INVALID");
    }

    @Test
    @DisplayName("긴급 공지는 배너로 나오고, 기간이 겹치는 긴급 공지는 등록할 수 없다")
    void urgentBanner() throws Exception {
        Member admin = newAdmin("관리자");
        Member user = newMember("회원");
        Long urgentId = create(admin, "URGENT", "긴급 점검 안내", now().minusMinutes(1), now().plusHours(1), null);

        Response active = get(user, "/notices/active").ok();
        assertThat(active.id("$.data.banner.noticeId")).isEqualTo(urgentId);

        post(admin, "/admin/notices", """
                {"noticeType":"URGENT","title":"또 긴급","content":"c","displayStartsAt":"%s","displayEndsAt":"%s"}"""
                .formatted(at(now().plusMinutes(10)), at(now().plusHours(2)))).fails(409, "ADMIN_NOTICE_BANNER_CONFLICT");
    }

    @Test
    @DisplayName("닫은 공지는 다시 나오지 않고, 같은 공지를 두 번 닫아도 정상 처리한다")
    void dismiss() throws Exception {
        Member admin = newAdmin("관리자");
        Member user = newMember("회원");
        Long noticeId = create(admin, "GENERAL", "일반 공지", now().minusMinutes(1), now().plusDays(1), null);

        List<Number> before = get(user, "/notices/active").ok().read("$.data.notices[*].noticeId");
        assertThat(before).extracting(Number::longValue).contains(noticeId);

        post(user, "/notices/" + noticeId + "/dismiss").ok();
        List<Number> after = get(user, "/notices/active").ok().read("$.data.notices[*].noticeId");
        assertThat(after).extracting(Number::longValue).doesNotContain(noticeId);

        post(user, "/notices/" + noticeId + "/dismiss").ok();
    }

    @Test
    @DisplayName("점검 공지는 닫았어도 점검 시작 전에는 다시 나온다")
    void maintenanceReappears() throws Exception {
        Member admin = newAdmin("관리자");
        Member user = newMember("회원");
        Long noticeId = create(admin, "MAINTENANCE", "점검 예정", now().minusMinutes(1), now().plusDays(2), now().plusDays(1));

        post(user, "/notices/" + noticeId + "/dismiss").ok();

        List<Number> active = get(user, "/notices/active").ok().read("$.data.notices[*].noticeId");
        assertThat(active).extracting(Number::longValue).contains(noticeId);
    }

    @Test
    @DisplayName("노출 기간이 지난 공지와 아직 시작 전인 공지는 노출 목록에 없다")
    void outsidePeriod() throws Exception {
        Member admin = newAdmin("관리자");
        Member user = newMember("회원");
        Long ended = create(admin, "GENERAL", "끝난 공지", now().minusDays(3), now().minusDays(1), null);
        Long scheduled = create(admin, "GENERAL", "예약 공지", now().plusDays(1), now().plusDays(3), null);

        List<Number> active = get(user, "/notices/active").ok().read("$.data.notices[*].noticeId");
        assertThat(active).extracting(Number::longValue).doesNotContain(ended, scheduled);

        Response adminList = get(admin, "/admin/notices?state=SCHEDULED&size=100").ok();
        List<Number> scheduledIds = adminList.read("$.data.content[*].noticeId");
        assertThat(scheduledIds).extracting(Number::longValue).contains(scheduled).doesNotContain(ended);
    }

    @Test
    @DisplayName("공지 상세·수정·삭제, 삭제하면 NOTICE_NOT_FOUND, 모두 감사 로그")
    void crud() throws Exception {
        Member admin = newAdmin("관리자");
        Member user = newMember("회원");
        Long noticeId = create(admin, "GENERAL", "원래 제목", now().minusMinutes(1), now().plusDays(1), null);

        Response detail = get(user, "/notices/" + noticeId).ok();
        assertThat(detail.<String>read("$.data.content")).isEqualTo("# 내용");

        Response updated = put(admin, "/admin/notices/" + noticeId, """
                {"noticeType":"GENERAL","title":"바뀐 제목","content":"새 내용","displayStartsAt":"%s","displayEndsAt":"%s"}"""
                .formatted(at(now().minusMinutes(1)), at(now().plusDays(2)))).ok();
        assertThat(updated.<String>read("$.data.title")).isEqualTo("바뀐 제목");

        delete(admin, "/admin/notices/" + noticeId).ok();
        get(user, "/notices/" + noticeId).fails(404, "NOTICE_NOT_FOUND");
        get(admin, "/admin/notices/" + noticeId).fails(404, "NOTICE_NOT_FOUND");

        Integer audits = jdbc.queryForObject("""
                SELECT count(*) FROM admin_audit_log WHERE admin_account_id = ? AND target_id = ?
                AND action_type IN ('NOTICE_CREATE', 'NOTICE_UPDATE', 'NOTICE_DELETE')
                """, Integer.class, admin.id(), noticeId);
        assertThat(audits).isEqualTo(3);
    }
}
