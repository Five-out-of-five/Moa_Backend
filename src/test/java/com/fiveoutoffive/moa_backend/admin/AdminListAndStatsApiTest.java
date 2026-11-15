package com.fiveoutoffive.moa_backend.admin;

import com.fiveoutoffive.moa_backend.global.security.GlobalJwtProvider;
import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 19. Admin — 목록 필터와 통계 수치. ({@link AdminAccountRoomApiTest} 에서 다루지 않은 부분)
 *
 * <ul>
 *   <li>[관리자] 신고 목록: status·targetType·reason 필터, overdue</li>
 *   <li>[관리자] 공지 상세</li>
 *   <li>[관리자] 회원 목록: provider·status·role·가입일 필터</li>
 *   <li>[관리자] 서비스 지표·AI 사용량: 실제 데이터와 수치가 맞는지</li>
 * </ul>
 *
 * <p>테스트끼리 같은 스키마를 쓰므로, 통계는 "조회 → 데이터 추가 → 다시 조회"의 차이로 확인한다.
 */
@DisplayName("19. Admin — 목록 필터·통계 수치")
class AdminListAndStatsApiTest extends Be2ApiTestSupport {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Autowired
    private GlobalJwtProvider jwtProvider;

    private Member newAdmin(String name) {
        Member m = newMember(name);
        jdbc.update("UPDATE account SET role = 'ADMIN' WHERE id = ?", m.id());
        return new Member(m.id(), name, jwtProvider.createAccessToken(String.valueOf(m.id()), "ADMIN"));
    }

    private Long newCard(Long roomId, Member writer, String title) throws Exception {
        if (get(writer, "/rooms/" + roomId + "/sessions/current").ok().<Object>read("$.data") == null) {
            post(writer, "/rooms/" + roomId + "/sessions", """
                    {"title":"1회차"}""").ok();
        }
        return post(writer, "/rooms/" + roomId + "/idea-cards", """
                {"cardType":"PROBLEM","title":"%s"}""".formatted(title)).ok().id("$.data.cardId");
    }

    private Long report(Member reporter, Long cardId, String reason) throws Exception {
        return post(reporter, "/reports", """
                {"targetType":"CARD","targetId":%d,"reason":"%s"}""".formatted(cardId, reason)).ok().id("$.data.reportId");
    }

    private List<Long> ids(Response res, String path) {
        List<Number> raw = res.read(path);
        return raw.stream().map(Number::longValue).toList();
    }

    // ---------- 신고 목록 ----------

    @Test
    @DisplayName("신고 목록: 상태·사유·대상 종류로 거르고, 7일 지난 미처리 건은 overdue=true")
    void reportListFilters() throws Exception {
        Member admin = newAdmin("관리자");
        Member reporter = newMember("신고자");
        Member writer = newMember("작성자");
        Long roomId = newRoom(reporter, writer);
        Long spam = report(reporter, newCard(roomId, writer, "스팸 카드"), "SPAM");
        Long abuse = report(reporter, newCard(roomId, writer, "욕설 카드"), "ABUSE");
        Long oldOne = report(reporter, newCard(roomId, writer, "오래된 카드"), "PRIVACY");
        jdbc.update("UPDATE report SET created_at = created_at - interval '8 days' WHERE id = ?", oldOne);
        post(admin, "/admin/reports/" + abuse + "/resolve", """
                {"action":"WARN","actionReason":"경고"}""").ok();

        List<Long> received = ids(get(admin, "/admin/reports?status=RECEIVED&size=100").ok(), "$.data.content[*].reportId");
        assertThat(received).contains(spam, oldOne).doesNotContain(abuse);

        List<Long> resolved = ids(get(admin, "/admin/reports?status=RESOLVED&size=100").ok(), "$.data.content[*].reportId");
        assertThat(resolved).contains(abuse).doesNotContain(spam, oldOne);

        List<Long> bySpam = ids(get(admin, "/admin/reports?reason=SPAM&size=100").ok(), "$.data.content[*].reportId");
        assertThat(bySpam).contains(spam).doesNotContain(abuse, oldOne);

        List<Long> messages = ids(get(admin, "/admin/reports?targetType=MESSAGE&size=100").ok(), "$.data.content[*].reportId");
        assertThat(messages).doesNotContain(spam, abuse, oldOne);

        Response list = get(admin, "/admin/reports?status=RECEIVED&size=100").ok();
        List<Boolean> overdueOfOld = list.read("$.data.content[?(@.reportId == %d)].overdue".formatted(oldOne));
        List<Boolean> overdueOfSpam = list.read("$.data.content[?(@.reportId == %d)].overdue".formatted(spam));
        assertThat(overdueOfOld).containsExactly(true);
        assertThat(overdueOfSpam).containsExactly(false);
        List<String> roomNames = list.read("$.data.content[?(@.reportId == %d)].room.name".formatted(spam));
        assertThat(roomNames).containsExactly("테스트방");
    }

    @Test
    @DisplayName("일반 회원은 신고 목록을 볼 수 없다")
    void reportListForbidden() throws Exception {
        get(newMember("회원"), "/admin/reports").fails(403, "COMMON_FORBIDDEN");
    }

    // ---------- 공지 상세 ----------

    @Test
    @DisplayName("관리자 공지 상세: 등록한 내용(Markdown 본문, 기간, 점검 시작 시각)을 그대로 돌려준다")
    void noticeDetail() throws Exception {
        Member admin = newAdmin("관리자");
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.ofHours(9)).truncatedTo(ChronoUnit.SECONDS);
        Long noticeId = post(admin, "/admin/notices", """
                {"noticeType":"MAINTENANCE","title":"서버 점검","content":"## 점검 안내\\n- 02:00~04:00",
                 "displayStartsAt":"%s","displayEndsAt":"%s","maintenanceStartsAt":"%s"}"""
                .formatted(now.minusHours(1), now.plusDays(2), now.plusDays(1))).ok().id("$.data.noticeId");

        Response detail = get(admin, "/admin/notices/" + noticeId).ok();

        assertThat(detail.<String>read("$.data.noticeType")).isEqualTo("MAINTENANCE");
        assertThat(detail.<String>read("$.data.title")).isEqualTo("서버 점검");
        assertThat(detail.<String>read("$.data.content")).isEqualTo("## 점검 안내\n- 02:00~04:00");
        assertThat(OffsetDateTime.parse(detail.read("$.data.displayStartsAt")).toInstant()).isEqualTo(now.minusHours(1).toInstant());
        assertThat(OffsetDateTime.parse(detail.read("$.data.maintenanceStartsAt")).toInstant()).isEqualTo(now.plusDays(1).toInstant());
        assertThat(detail.<String>read("$.data.maintenanceStartsAt")).endsWith("+09:00");
    }

    // ---------- 회원 목록 필터 ----------

    @Test
    @DisplayName("회원 목록: 가입 제공자·상태·역할·가입일(한국 날짜)로 거른다")
    void accountListFilters() throws Exception {
        Member admin = newAdmin("필터관리자");
        String tag = "필터" + UUID.randomUUID().toString().substring(0, 8);
        Long kakao = jdbc.queryForObject("""
                INSERT INTO account(provider, provider_uid, email, name, terms_agreed_at, privacy_agreed_at)
                VALUES ('KAKAO', ?, ?, ?, now(), now()) RETURNING id
                """, Long.class, UUID.randomUUID().toString(), UUID.randomUUID() + "@be1.test", tag + "카카오");
        Member suspended = newMember(tag + "정지");
        jdbc.update("UPDATE account SET status = 'SUSPENDED', suspended_until = (now() AT TIME ZONE 'UTC') + interval '3 days' WHERE id = ?", suspended.id());
        Member old = newMember(tag + "옛날회원");
        jdbc.update("UPDATE account SET created_at = TIMESTAMP '2026-01-10 03:00:00' WHERE id = ?", old.id());
        String keyword = "&keyword=" + tag + "&size=100";

        assertThat(ids(get(admin, "/admin/accounts?provider=KAKAO" + keyword).ok(), "$.data.content[*].accountId"))
                .containsExactly(kakao);
        assertThat(ids(get(admin, "/admin/accounts?status=SUSPENDED" + keyword).ok(), "$.data.content[*].accountId"))
                .containsExactly(suspended.id());
        assertThat(ids(get(admin, "/admin/accounts?role=ADMIN&size=100").ok(), "$.data.content[*].accountId"))
                .contains(admin.id()).doesNotContain(kakao, suspended.id());
        // 2026-01-10 03:00 UTC = 한국 날짜 2026-01-10
        assertThat(ids(get(admin, "/admin/accounts?joinedFrom=2026-01-10&joinedTo=2026-01-10" + keyword).ok(), "$.data.content[*].accountId"))
                .containsExactly(old.id());
        assertThat(ids(get(admin, "/admin/accounts?joinedFrom=2026-01-11" + keyword).ok(), "$.data.content[*].accountId"))
                .doesNotContain(old.id()).contains(kakao, suspended.id());
    }

    // ---------- 통계 수치 ----------

    @Test
    @DisplayName("서비스 지표: 오늘 가입·개설·활동한 수가 그만큼 늘어난다")
    void overviewNumbers() throws Exception {
        Member admin = newAdmin("통계관리자");
        String today = LocalDate.now(SEOUL).toString();
        String path = "/admin/stats/overview?unit=DAY&from=" + today + "&to=" + today;
        Response before = get(admin, path).ok();

        Member a = newMember("오늘가입1");
        newMember("오늘가입2");
        newRoom(a);
        get(a, "/accounts/me").ok();

        Response after = get(admin, path).ok();
        assertThat(after.<Integer>read("$.data.series[0].newAccounts") - before.<Integer>read("$.data.series[0].newAccounts")).isEqualTo(2);
        assertThat(after.<Integer>read("$.data.series[0].newRooms") - before.<Integer>read("$.data.series[0].newRooms")).isEqualTo(1);
        assertThat(after.<Integer>read("$.data.series[0].activeAccounts") - before.<Integer>read("$.data.series[0].activeAccounts")).isGreaterThanOrEqualTo(1);
        assertThat(after.<Integer>read("$.data.totals.accounts") - before.<Integer>read("$.data.totals.accounts")).isEqualTo(2);
        assertThat(after.<Integer>read("$.data.totals.rooms") - before.<Integer>read("$.data.totals.rooms")).isEqualTo(1);
        assertThat(after.<String>read("$.data.series[0].periodStart")).isEqualTo(today);
    }

    @Test
    @DisplayName("서비스 지표: WEEK·MONTH 단위로 기간을 나눈다")
    void overviewUnits() throws Exception {
        Member admin = newAdmin("통계관리자");

        Response weeks = get(admin, "/admin/stats/overview?unit=WEEK&from=2026-10-05&to=2026-10-25").ok();
        assertThat(weeks.<List<String>>read("$.data.series[*].periodStart")).containsExactly("2026-10-05", "2026-10-12", "2026-10-19");

        Response months = get(admin, "/admin/stats/overview?unit=MONTH&from=2026-08-01&to=2026-10-31").ok();
        assertThat(months.<List<String>>read("$.data.series[*].periodStart")).containsExactly("2026-08-01", "2026-09-01", "2026-10-01");
    }

    @Test
    @DisplayName("AI 사용량: 기능별 호출·실패·토큰·비용 합계가 기록과 맞는다")
    void aiUsageNumbers() throws Exception {
        Member admin = newAdmin("통계관리자");
        String today = LocalDate.now(SEOUL).toString();
        String path = "/admin/stats/ai-usage?unit=DAY&from=" + today + "&to=" + today + "&feature=TASK_RECOMMEND";
        Usage before = usage(get(admin, path).ok());

        String insert = """
                INSERT INTO ai_call_log(feature, prompt_tokens, output_tokens, cost_amount, status)
                VALUES ('TASK_RECOMMEND', ?, ?, ?, ?)
                """;
        jdbc.update(insert, 100, 20, new BigDecimal("0.0100"), "SUCCESS");
        jdbc.update(insert, 50, 10, new BigDecimal("0.0050"), "SUCCESS");
        jdbc.update(insert, null, null, null, "FAILED");

        Response res = get(admin, path).ok();
        Usage after = usage(res);
        assertThat(after.calls - before.calls).isEqualTo(3);
        assertThat(after.failed - before.failed).isEqualTo(1);
        assertThat(after.prompt - before.prompt).isEqualTo(150);
        assertThat(after.output - before.output).isEqualTo(30);
        assertThat(after.cost.subtract(before.cost)).isEqualByComparingTo("0.0150");
        List<String> features = res.read("$.data.byFeature[*].feature");
        assertThat(features).containsOnly("TASK_RECOMMEND");
    }

    private record Usage(long calls, long failed, long prompt, long output, BigDecimal cost) {
    }

    /** feature 로 거른 응답에서 해당 기능 한 줄을 읽는다. 기록이 없으면 0. */
    private Usage usage(Response res) {
        List<Object> rows = res.read("$.data.byFeature");
        if (rows.isEmpty()) {
            return new Usage(0, 0, 0, 0, BigDecimal.ZERO);
        }
        Number cost = res.read("$.data.byFeature[0].costAmount");
        return new Usage(res.<Number>read("$.data.byFeature[0].calls").longValue(), res.<Number>read("$.data.byFeature[0].failedCalls").longValue(),
                res.<Number>read("$.data.byFeature[0].promptTokens").longValue(), res.<Number>read("$.data.byFeature[0].outputTokens").longValue(),
                new BigDecimal(cost.toString()));
    }
}
