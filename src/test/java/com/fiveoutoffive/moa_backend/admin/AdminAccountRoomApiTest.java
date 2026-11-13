package com.fiveoutoffive.moa_backend.admin;

import com.fiveoutoffive.moa_backend.global.security.GlobalJwtProvider;
import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 19. Admin — 회원 관리(5)·방 관리(4)·통계(2)·감사 로그(1). 신고·공지 관리는 각 도메인 테스트에 있다.
 *
 * <p>관리자 API 는 /api/v1/admin/** → hasRole('ADMIN') 한 줄로 막고, 변경 조작은 모두 admin_audit_log 에 남긴다(API 명세 공통 규칙).
 */
@DisplayName("19. Admin — 회원·방·통계·감사 로그")
class AdminAccountRoomApiTest extends Be2ApiTestSupport {

    @Autowired
    private GlobalJwtProvider jwtProvider;

    /** account.role 과 토큰의 role 클레임을 모두 ADMIN 으로 둔 관리자. */
    private Member newAdmin(String name) {
        Member m = newMember(name);
        jdbc.update("UPDATE account SET role = 'ADMIN' WHERE id = ?", m.id());
        return new Member(m.id(), name, jwtProvider.createAccessToken(String.valueOf(m.id()), "ADMIN"));
    }

    private int auditCount(Long adminId, String actionType, Long targetId) {
        return jdbc.queryForObject("SELECT count(*) FROM admin_audit_log WHERE admin_account_id = ? AND action_type = ? AND target_id = ?",
                Integer.class, adminId, actionType, targetId);
    }

    @Test
    @DisplayName("일반 회원은 관리자 API 를 쓸 수 없다 (COMMON_FORBIDDEN)")
    void userForbidden() throws Exception {
        Member user = newMember("일반회원");

        get(user, "/admin/accounts").fails(403, "COMMON_FORBIDDEN");
        get(user, "/admin/rooms").fails(403, "COMMON_FORBIDDEN");
    }

    @Nested
    @DisplayName("회원 관리")
    class Accounts {

        @Test
        @DisplayName("이름으로 검색하고 상세에서 참여 방 수를 본다")
        void searchAndDetail() throws Exception {
            Member admin = newAdmin("관리자");
            Member target = newMember("검색될회원");
            newRoom(target);

            Response list = get(admin, "/admin/accounts?keyword=검색될회원").ok();
            List<Number> ids = list.read("$.data.content[*].accountId");
            assertThat(ids).extracting(Number::longValue).contains(target.id());

            Response detail = get(admin, "/admin/accounts/" + target.id()).ok();
            assertThat(detail.<String>read("$.data.name")).isEqualTo("검색될회원");
            assertThat(detail.<Integer>read("$.data.roomCount")).isEqualTo(1);
            assertThat(detail.<Integer>read("$.data.ownedRoomCount")).isEqualTo(1);

            get(admin, "/admin/accounts/999999999").fails(404, "ACCOUNT_NOT_FOUND");
        }

        @Test
        @DisplayName("정지하면 그 회원의 모든 API 가 AUTH_ACCOUNT_SUSPENDED, 해제하면 다시 쓸 수 있다. 둘 다 감사 로그")
        void suspendAndRelease() throws Exception {
            Member admin = newAdmin("관리자");
            Member target = newMember("정지대상");

            Response suspended = post(admin, "/admin/accounts/" + target.id() + "/suspension", """
                    {"period":"DAYS_7","reason":"욕설"}""").ok();
            assertThat(suspended.<String>read("$.data.status")).isEqualTo("SUSPENDED");
            assertThat(suspended.<String>read("$.data.suspendedUntil")).isNotNull();
            assertThat(suspended.<String>read("$.data.suspensions[0].period")).isEqualTo("DAYS_7");

            get(target, "/accounts/me").fails(403, "AUTH_ACCOUNT_SUSPENDED");
            post(admin, "/admin/accounts/" + target.id() + "/suspension", """
                    {"period":"DAYS_3","reason":"또"}""").fails(409, "ADMIN_ALREADY_SUSPENDED");

            Response released = delete(admin, "/admin/accounts/" + target.id() + "/suspension").ok();
            assertThat(released.<String>read("$.data.status")).isEqualTo("ACTIVE");
            assertThat(released.<String>read("$.data.suspensions[0].releasedAt")).isNotNull();
            get(target, "/accounts/me").ok();
            delete(admin, "/admin/accounts/" + target.id() + "/suspension").fails(409, "ADMIN_NOT_SUSPENDED");

            assertThat(auditCount(admin.id(), "ACCOUNT_SUSPEND", target.id())).isEqualTo(1);
            assertThat(auditCount(admin.id(), "ACCOUNT_RELEASE", target.id())).isEqualTo(1);
        }

        @Test
        @DisplayName("영구 정지는 종료 시각이 없다")
        void permanent() throws Exception {
            Member admin = newAdmin("관리자");
            Member target = newMember("영구정지");

            Response res = post(admin, "/admin/accounts/" + target.id() + "/suspension", """
                    {"period":"PERMANENT","reason":"반복 위반"}""").ok();

            assertThat(res.<String>read("$.data.suspendedUntil")).isNull();
            get(target, "/accounts/me").fails(403, "AUTH_ACCOUNT_SUSPENDED");
        }

        @Test
        @DisplayName("관리자·탈퇴 회원은 정지할 수 없다")
        void cannotSuspend() throws Exception {
            Member admin = newAdmin("관리자");
            Member otherAdmin = newAdmin("다른관리자");
            Member withdrawn = newMember("탈퇴회원");
            jdbc.update("UPDATE account SET status = 'WITHDRAWN', deleted_at = now() WHERE id = ?", withdrawn.id());

            post(admin, "/admin/accounts/" + otherAdmin.id() + "/suspension", """
                    {"period":"DAYS_3","reason":"x"}""").fails(409, "ADMIN_CANNOT_SUSPEND_ADMIN");
            post(admin, "/admin/accounts/" + withdrawn.id() + "/suspension", """
                    {"period":"DAYS_3","reason":"x"}""").fails(409, "ADMIN_ACCOUNT_WITHDRAWN");
        }

        @Test
        @DisplayName("관리자 권한 부여·해제, 본인 권한은 바꿀 수 없다")
        void changeRole() throws Exception {
            Member admin = newAdmin("관리자");
            Member target = newMember("승격대상");

            Response promoted = put(admin, "/admin/accounts/" + target.id() + "/role", """
                    {"role":"ADMIN"}""").ok();
            assertThat(promoted.<String>read("$.data.role")).isEqualTo("ADMIN");
            Response demoted = put(admin, "/admin/accounts/" + target.id() + "/role", """
                    {"role":"USER"}""").ok();
            assertThat(demoted.<String>read("$.data.role")).isEqualTo("USER");

            put(admin, "/admin/accounts/" + admin.id() + "/role", """
                    {"role":"USER"}""").fails(409, "ADMIN_CANNOT_CHANGE_OWN_ROLE");
            assertThat(auditCount(admin.id(), "ROLE_CHANGE", target.id())).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("방 관리")
    class Rooms {

        @Test
        @DisplayName("비활성화하면 멤버는 ROOM_INACTIVE, 다시 활성화하면 들어갈 수 있다")
        void inactivateAndReactivate() throws Exception {
            Member admin = newAdmin("관리자");
            Member owner = newMember("방장");
            Long roomId = newRoom(owner);

            Response off = post(admin, "/admin/rooms/" + roomId + "/inactivation", """
                    {"reason":"신고 누적"}""").ok();
            assertThat(off.<String>read("$.data.status")).isEqualTo("INACTIVE");
            assertThat(off.<String>read("$.data.inactiveReason")).isEqualTo("신고 누적");
            get(owner, "/rooms/" + roomId).fails(403, "ROOM_INACTIVE");
            post(admin, "/admin/rooms/" + roomId + "/inactivation", """
                    {"reason":"또"}""").fails(409, "ADMIN_ROOM_ALREADY_INACTIVE");

            Response on = delete(admin, "/admin/rooms/" + roomId + "/inactivation").ok();
            assertThat(on.<String>read("$.data.status")).isEqualTo("ACTIVE");
            get(owner, "/rooms/" + roomId).ok();
            delete(admin, "/admin/rooms/" + roomId + "/inactivation").fails(409, "ADMIN_ROOM_NOT_INACTIVE");

            assertThat(auditCount(admin.id(), "ROOM_INACTIVATE", roomId)).isEqualTo(1);
            assertThat(auditCount(admin.id(), "ROOM_REACTIVATE", roomId)).isEqualTo(1);
        }

        @Test
        @DisplayName("방 목록은 이름으로 검색하고, 상세는 없는 방이면 ROOM_NOT_FOUND")
        void listAndDetail() throws Exception {
            Member admin = newAdmin("관리자");
            Long roomId = newRoom(newMember("방장"));

            Response list = get(admin, "/admin/rooms?keyword=테스트방&size=100").ok();
            List<Number> ids = list.read("$.data.content[*].roomId");
            assertThat(ids).extracting(Number::longValue).contains(roomId);

            Response detail = get(admin, "/admin/rooms/" + roomId).ok();
            assertThat(detail.<String>read("$.data.creator.name")).isEqualTo("방장");
            assertThat(detail.<Integer>read("$.data.memberCount")).isEqualTo(1);

            get(admin, "/admin/rooms/999999999").fails(404, "ROOM_NOT_FOUND");
        }

        @Test
        @DisplayName("방 목록 sort=REPORT_COUNT 면 신고가 많은 방이 먼저 나온다")
        void sortByReportCount() throws Exception {
            Member admin = newAdmin("관리자");
            Member reporter = newMember("신고자");
            // 신고 많은 방을 먼저(더 오래전에) 만든다. 생성일 최신순으로 정렬되면 이 방이 뒤로 가서 실패한다.
            Long noisy = newRoom(newMember("신고많은방장"));
            Long quiet = newRoom(newMember("조용한방장"));
            for (int i = 0; i < 3; i++) {
                jdbc.update("""
                        INSERT INTO report(reporter_account_id, room_id, target_type, target_id, reason, status, snapshot_json, is_false_report)
                        VALUES (?, ?, 'CARD', ?, 'SPAM', 'RECEIVED', '{}', false)
                        """, reporter.id(), noisy, 900000 + i);
            }

            Response list = get(admin, "/admin/rooms?sort=REPORT_COUNT&size=100").ok();
            List<Number> ids = list.read("$.data.content[*].roomId");
            List<Long> order = ids.stream().map(Number::longValue).toList();
            assertThat(order.indexOf(noisy)).as("신고 3건인 방이 신고 0건인 방보다 앞").isLessThan(order.indexOf(quiet));
        }
    }

    @Nested
    @DisplayName("통계·감사 로그")
    class StatsAndAudit {

        @Test
        @DisplayName("서비스 지표는 집계값만, 데이터가 없는 날도 0 으로 채운다")
        void overview() throws Exception {
            Member admin = newAdmin("관리자");
            LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"));

            Response res = get(admin, "/admin/stats/overview?unit=DAY&from=" + today.minusDays(2) + "&to=" + today).ok();

            assertThat(res.<Integer>read("$.data.totals.accounts")).isPositive();
            assertThat(res.<List<Object>>read("$.data.series")).hasSize(3);
        }

        @Test
        @DisplayName("AI 사용량 조회")
        void aiUsage() throws Exception {
            Member admin = newAdmin("관리자");
            LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"));

            Response res = get(admin, "/admin/stats/ai-usage?unit=MONTH&from=" + today.minusMonths(1) + "&to=" + today).ok();

            assertThat(res.<Object>read("$.data.byFeature")).isNotNull();
            assertThat(res.<Object>read("$.data.threshold")).isNotNull();
        }

        @Test
        @DisplayName("감사 로그를 관리자·조작 종류로 거른다")
        void auditLogs() throws Exception {
            Member admin = newAdmin("감사관리자");
            Member target = newMember("대상");
            post(admin, "/admin/accounts/" + target.id() + "/suspension", """
                    {"period":"DAYS_3","reason":"테스트"}""").ok();

            Response logs = get(admin, "/admin/audit-logs?adminId=" + admin.id() + "&actionType=ACCOUNT_SUSPEND").ok();

            assertThat(logs.<Integer>read("$.data.totalElements")).isEqualTo(1);
            assertThat(logs.id("$.data.content[0].targetId")).isEqualTo(target.id());
            assertThat(logs.<String>read("$.data.content[0].admin.name")).isEqualTo("감사관리자");
        }
    }
}
