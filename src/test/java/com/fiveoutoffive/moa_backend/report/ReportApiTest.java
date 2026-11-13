package com.fiveoutoffive.moa_backend.report;

import com.fiveoutoffive.moa_backend.global.security.GlobalJwtProvider;
import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 17. Report(신고 접수 1) + 19. Admin 신고 처리(5). 요구사항 76·77.
 */
@DisplayName("17. Report · 19. Admin 신고 처리")
class ReportApiTest extends Be2ApiTestSupport {

    @Autowired
    private GlobalJwtProvider jwtProvider;

    private Member newAdmin(String name) {
        Member m = newMember(name);
        jdbc.update("UPDATE account SET role = 'ADMIN' WHERE id = ?", m.id());
        return new Member(m.id(), name, jwtProvider.createAccessToken(String.valueOf(m.id()), "ADMIN"));
    }

    /** 방 안에서 writer 가 쓴 아이디어 카드. 진행 중 회차가 없으면 연다. */
    private Long newCard(Long roomId, Member writer, String title) throws Exception {
        if (get(writer, "/rooms/" + roomId + "/sessions/current").ok().<Object>read("$.data") == null) {
            post(writer, "/rooms/" + roomId + "/sessions", """
                    {"title":"1회차"}""").ok();
        }
        return post(writer, "/rooms/" + roomId + "/idea-cards", """
                {"cardType":"PROBLEM","title":"%s","content":"본문"}""".formatted(title)).ok().id("$.data.cardId");
    }

    private Long report(Member reporter, Long cardId) throws Exception {
        return post(reporter, "/reports", """
                {"targetType":"CARD","targetId":%d,"reason":"ABUSE","detail":"욕설이 있어요"}""".formatted(cardId)).ok().id("$.data.reportId");
    }

    @Nested
    @DisplayName("신고 접수")
    class Create {

        @Test
        @DisplayName("같은 방 멤버의 카드를 신고하면 RECEIVED, 같은 대상은 다시 신고할 수 없다")
        void createAndDuplicate() throws Exception {
            Member reporter = newMember("신고자");
            Member writer = newMember("작성자");
            Long roomId = newRoom(reporter, writer);
            Long cardId = newCard(roomId, writer, "문제 카드");

            Response res = post(reporter, "/reports", """
                    {"targetType":"CARD","targetId":%d,"reason":"ABUSE"}""".formatted(cardId)).ok();
            assertThat(res.<String>read("$.data.status")).isEqualTo("RECEIVED");

            post(reporter, "/reports", """
                    {"targetType":"CARD","targetId":%d,"reason":"SPAM"}""".formatted(cardId)).fails(409, "REPORT_DUPLICATED");
        }

        @Test
        @DisplayName("본인 글·없는 대상·다른 방 사람은 신고할 수 없다")
        void rules() throws Exception {
            Member writer = newMember("작성자");
            Member outsider = newMember("외부인");
            Long roomId = newRoom(writer);
            Long cardId = newCard(roomId, writer, "내 카드");

            post(writer, "/reports", """
                    {"targetType":"CARD","targetId":%d,"reason":"ETC"}""".formatted(cardId)).fails(400, "REPORT_OWN_CONTENT");
            post(outsider, "/reports", """
                    {"targetType":"CARD","targetId":%d,"reason":"ETC"}""".formatted(cardId)).fails(403, "ROOM_NOT_MEMBER");
            post(writer, "/reports", """
                    {"targetType":"CARD","targetId":999999999,"reason":"ETC"}""").fails(404, "REPORT_TARGET_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("관리자 처리")
    class Process {

        @Test
        @DisplayName("상세에는 신고 시점 스냅샷이 있고, 처리 시작 → 콘텐츠 삭제 조치 → 다시 처리 불가")
        void reviewAndDeleteContent() throws Exception {
            Member admin = newAdmin("관리자");
            Member reporter = newMember("신고자");
            Member writer = newMember("작성자");
            Long roomId = newRoom(reporter, writer);
            Long cardId = newCard(roomId, writer, "삭제될 카드");
            Long reportId = report(reporter, cardId);

            Response detail = get(admin, "/admin/reports/" + reportId).ok();
            assertThat(detail.<String>read("$.data.snapshot")).contains("삭제될 카드");
            assertThat(detail.<String>read("$.data.targetAccount.name")).isEqualTo("작성자");

            assertThat(post(admin, "/admin/reports/" + reportId + "/review").ok().<String>read("$.data.status")).isEqualTo("IN_PROGRESS");
            Response resolved = post(admin, "/admin/reports/" + reportId + "/resolve", """
                    {"action":"DELETE_CONTENT","actionReason":"욕설 확인"}""").ok();
            assertThat(resolved.<String>read("$.data.status")).isEqualTo("RESOLVED");
            assertThat(resolved.<Boolean>read("$.data.targetDeleted")).isTrue();
            get(reporter, "/rooms/" + roomId + "/idea-cards/" + cardId).fails(404, "CARD_NOT_FOUND");

            post(admin, "/admin/reports/" + reportId + "/reject", """
                    {"actionReason":"다시"}""").fails(409, "ADMIN_REPORT_ALREADY_PROCESSED");
        }

        @Test
        @DisplayName("계정 정지 조치는 기간이 필수이고, 작성자를 정지하며 정지 이력에 신고가 연결된다")
        void suspendAccount() throws Exception {
            Member admin = newAdmin("관리자");
            Member reporter = newMember("신고자");
            Member writer = newMember("작성자");
            Long roomId = newRoom(reporter, writer);
            Long reportId = report(reporter, newCard(roomId, writer, "카드"));

            post(admin, "/admin/reports/" + reportId + "/resolve", """
                    {"action":"SUSPEND_ACCOUNT","actionReason":"반복"}""").fails(400, "COMMON_INVALID_INPUT");
            post(admin, "/admin/reports/" + reportId + "/resolve", """
                    {"action":"SUSPEND_ACCOUNT","actionReason":"반복","suspensionPeriod":"DAYS_3"}""").ok();

            get(writer, "/accounts/me").fails(403, "AUTH_ACCOUNT_SUSPENDED");
            Long linked = jdbc.queryForObject("SELECT report_id FROM account_suspension WHERE account_id = ?", Long.class, writer.id());
            assertThat(linked).isEqualTo(reportId);
        }

        @Test
        @DisplayName("조치 없음이어도 사유는 필수")
        void reasonRequired() throws Exception {
            Member admin = newAdmin("관리자");
            Member reporter = newMember("신고자");
            Member writer = newMember("작성자");
            Long roomId = newRoom(reporter, writer);
            Long reportId = report(reporter, newCard(roomId, writer, "카드"));

            post(admin, "/admin/reports/" + reportId + "/resolve", """
                    {"action":"NONE","actionReason":""}""").fails(400, "COMMON_INVALID_INPUT");
            post(admin, "/admin/reports/" + reportId + "/resolve", """
                    {"action":"NONE","actionReason":"문제 없음"}""").ok();
        }

        @Test
        @DisplayName("허위 신고가 기준(30일 3회)에 닿으면 신고자의 신고 기능을 막는다")
        void falseReportBlock() throws Exception {
            Member admin = newAdmin("관리자");
            Member reporter = newMember("허위신고자");
            Member writer = newMember("작성자");
            Long roomId = newRoom(reporter, writer);
            for (int i = 0; i < 3; i++) {
                Long reportId = report(reporter, newCard(roomId, writer, "카드" + i));
                post(admin, "/admin/reports/" + reportId + "/reject", """
                        {"actionReason":"허위","falseReport":true}""").ok();
            }

            Response detail = get(admin, "/admin/accounts/" + reporter.id()).ok();
            assertThat(detail.<String>read("$.data.reportBlockedUntil")).isNotNull();
            post(reporter, "/reports", """
                    {"targetType":"CARD","targetId":%d,"reason":"ETC"}""".formatted(newCard(roomId, writer, "새 카드"))).fails(403, "REPORT_BLOCKED");
        }

        @Test
        @DisplayName("접수 후 7일 지난 미처리 건은 overdue=true")
        void overdue() throws Exception {
            Member admin = newAdmin("관리자");
            Member reporter = newMember("신고자");
            Member writer = newMember("작성자");
            Long roomId = newRoom(reporter, writer);
            Long reportId = report(reporter, newCard(roomId, writer, "오래된 신고"));
            jdbc.update("UPDATE report SET created_at = created_at - interval '8 days' WHERE id = ?", reportId);

            assertThat(get(admin, "/admin/reports/" + reportId).ok().<Boolean>read("$.data.overdue")).isTrue();
        }

        @Test
        @DisplayName("없는 신고는 REPORT_NOT_FOUND")
        void notFound() throws Exception {
            get(newAdmin("관리자"), "/admin/reports/999999999").fails(404, "REPORT_NOT_FOUND");
        }
    }
}
