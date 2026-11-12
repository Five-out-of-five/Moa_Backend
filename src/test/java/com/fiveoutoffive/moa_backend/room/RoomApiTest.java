package com.fiveoutoffive.moa_backend.room;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 04. Room — 방 개설·조회·설정·삭제, 초대 링크, 참여 요청·승인·거절, 멤버·방별 프로필·나가기·내보내기·위임 (API 18개).
 *
 * <p>기준은 API 명세 '04. Room' 행과 요구사항 7~15, 58, 61 이다.
 * 실행 조건은 {@link Be2ApiTestSupport} 와 같다(로컬 PostgreSQL + Redis, be2_test 스키마, Redis 15번).
 */
@DisplayName("04. Room")
class RoomApiTest extends Be2ApiTestSupport {

    // ---------- 도우미 ----------

    /** 방 개설 API(multipart, request 파트만)로 방을 만든다. */
    private Response createRoom(Member who, String requestJson) throws Exception {
        var req = MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/v1/rooms")
                .file(new MockMultipartFile("request", "", "application/json", requestJson.getBytes(StandardCharsets.UTF_8)))
                .header("Authorization", "Bearer " + who.token());
        var result = mockMvc.perform(req).andReturn().getResponse();
        result.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return new Response(result.getStatus(), result.getContentAsString());
    }

    private String inviteCode(Member owner, Long roomId) throws Exception {
        return get(owner, "/rooms/" + roomId + "/invite-link").ok().read("$.data.inviteCode");
    }

    private Long requestJoin(Member joiner, String code) throws Exception {
        return post(joiner, "/invites/" + code + "/join-requests").ok().id("$.data.requestId");
    }

    /** 초대 링크 → 참여 요청 → 방장 승인까지 실제 API 로 거친다. */
    private void join(Member owner, Long roomId, Member joiner) throws Exception {
        Long requestId = requestJoin(joiner, inviteCode(owner, roomId));
        post(owner, "/rooms/" + roomId + "/join-requests/" + requestId + "/approve").ok();
    }

    private Long newFile(Member uploader, Long roomId, String group) {
        return jdbc.queryForObject("""
                INSERT INTO file(owner_account_id, room_id, file_group, object_key, original_name, mime_type, size_bytes)
                VALUES (?, ?, ?, ?, 'image.png', 'image/png', 10) RETURNING id
                """, Long.class, uploader.id(), roomId, group, "test/" + java.util.UUID.randomUUID());
    }

    // ---------- 방 ----------

    @Nested
    @DisplayName("방 개설·조회")
    class Create {

        @Test
        @DisplayName("개설하면 개설자가 방장이고, 기본 위젯 4개·기본 채널이 함께 만들어진다")
        void createWithDefaults() throws Exception {
            Member owner = newMember("방장");

            Response res = createRoom(owner, """
                    {"name":"우리 팀","description":"설명"}""").ok();
            Long roomId = res.id("$.data.roomId");

            assertThat(res.<String>read("$.data.myMembership.role")).isEqualTo("OWNER");
            assertThat(res.<Integer>read("$.data.memberCount")).isEqualTo(1);
            assertThat(res.<Integer>read("$.data.maxMemberCount")).isEqualTo(20);
            assertThat(res.<Boolean>read("$.data.needsProfileSetup")).isFalse();

            List<String> widgets = get(owner, "/rooms/" + roomId + "/dashboard").ok().read("$.data.widgets[*].widgetType");
            assertThat(widgets).containsExactlyInAnyOrder("MOOD", "IDEA_BOARD", "CATCHUP_BRIEFING", "TASK_BOARD");

            List<Boolean> defaults = get(owner, "/rooms/" + roomId + "/channels").ok().read("$.data[*].isDefault");
            assertThat(defaults).contains(true);
        }

        @Test
        @DisplayName("방 이름이 2자 미만이면 COMMON_INVALID_INPUT")
        void nameTooShort() throws Exception {
            Member owner = newMember("방장");

            createRoom(owner, """
                    {"name":"가"}""").fails(400, "COMMON_INVALID_INPUT");
        }

        @Test
        @DisplayName("1인당 10개까지만 개설할 수 있다")
        void createLimit() throws Exception {
            Member owner = newMember("방장");
            for (int i = 0; i < 10; i++) {
                createRoom(owner, """
                        {"name":"방 %d"}""".formatted(i)).ok();
            }

            createRoom(owner, """
                    {"name":"열한 번째"}""").fails(409, "ROOM_CREATE_LIMIT_EXCEEDED");
        }

        @Test
        @DisplayName("내 방 목록에는 참여 중인 방만 역할과 함께 나온다")
        void myRooms() throws Exception {
            Member owner = newMember("방장");
            Member member = newMember("멤버");
            Long roomA = newRoom(owner, member);
            Long roomB = newRoom(owner);

            Response mine = get(member, "/rooms").ok();

            List<Number> ids = mine.read("$.data[*].roomId");
            assertThat(ids).extracting(Number::longValue).containsExactly(roomA).doesNotContain(roomB);
            assertThat(mine.<String>read("$.data[0].myRole")).isEqualTo("MEMBER");
            assertThat(mine.<Integer>read("$.data[0].memberCount")).isEqualTo(2);
        }

        @Test
        @DisplayName("방 멤버가 아니면 방 정보를 볼 수 없다")
        void notMember() throws Exception {
            Member owner = newMember("방장");
            Long roomId = newRoom(owner);

            get(newMember("외부인"), "/rooms/" + roomId).fails(403, "ROOM_NOT_MEMBER");
        }

        @Test
        @DisplayName("비활성 방은 ROOM_INACTIVE")
        void inactive() throws Exception {
            Member owner = newMember("방장");
            Long roomId = newRoom(owner);
            jdbc.update("UPDATE room SET status = 'INACTIVE' WHERE id = ?", roomId);

            get(owner, "/rooms/" + roomId).fails(403, "ROOM_INACTIVE");
        }
    }

    @Nested
    @DisplayName("방 설정 변경·삭제")
    class UpdateDelete {

        @Test
        @DisplayName("방장만 설정을 바꿀 수 있다")
        void ownerOnly() throws Exception {
            Member owner = newMember("방장");
            Member member = newMember("멤버");
            Long roomId = newRoom(owner, member);

            patch(member, "/rooms/" + roomId, """
                    {"name":"새 이름"}""").fails(403, "ROOM_OWNER_ONLY");
        }

        @Test
        @DisplayName("보낸 필드만 바꾸고, 보내지 않은 대표 이미지는 그대로 둔다")
        void partialUpdateKeepsImage() throws Exception {
            Member owner = newMember("방장");
            Long roomId = newRoom(owner);
            Long imageId = newFile(owner, roomId, "ROOM_IMAGE");

            Response withImage = patch(owner, "/rooms/" + roomId, """
                    {"imageFileId":%d}""".formatted(imageId)).ok();
            assertThat(withImage.<String>read("$.data.imageUrl")).isNotNull();

            Response renamed = patch(owner, "/rooms/" + roomId, """
                    {"name":"새 이름"}""").ok();
            assertThat(renamed.<String>read("$.data.name")).isEqualTo("새 이름");
            assertThat(renamed.<String>read("$.data.description")).isEqualTo("BE-2 통합 테스트");
            assertThat(renamed.<String>read("$.data.imageUrl")).as("imageFileId 를 보내지 않았으므로 이미지 유지").isNotNull();

            Response cleared = patch(owner, "/rooms/" + roomId, """
                    {"imageFileId":null}""").ok();
            assertThat(cleared.<String>read("$.data.imageUrl")).as("imageFileId=null 이면 이미지 삭제").isNull();
        }

        @Test
        @DisplayName("대표 이미지는 ROOM_IMAGE 용도 파일만")
        void imagePurpose() throws Exception {
            Member owner = newMember("방장");
            Long roomId = newRoom(owner);
            Long attachment = newFile(owner, roomId, "CHAT_ATTACHMENT");

            patch(owner, "/rooms/" + roomId, """
                    {"imageFileId":%d}""".formatted(attachment)).fails(400, "FILE_PURPOSE_MISMATCH");
        }

        @Test
        @DisplayName("방 이름이 다르면 삭제하지 않고, 맞으면 삭제 후 ROOM_NOT_FOUND")
        void delete() throws Exception {
            Member owner = newMember("방장");
            Long roomId = newRoom(owner);

            send(owner, HttpMethod.DELETE, "/rooms/" + roomId, """
                    {"confirmName":"다른 이름"}""").fails(400, "ROOM_DELETE_CONFIRM_MISMATCH");

            send(owner, HttpMethod.DELETE, "/rooms/" + roomId, """
                    {"confirmName":"테스트방"}""").ok();

            get(owner, "/rooms/" + roomId).fails(404, "ROOM_NOT_FOUND");
            List<Number> ids = get(owner, "/rooms").ok().read("$.data[*].roomId");
            assertThat(ids).extracting(Number::longValue).doesNotContain(roomId);
        }
    }

    // ---------- 초대·참여 ----------

    @Nested
    @DisplayName("초대 링크·참여 요청")
    class Invite {

        @Test
        @DisplayName("초대 링크는 방장만 보고, 주소는 {FRONTEND_URL}/invite/{code}")
        void inviteLink() throws Exception {
            Member owner = newMember("방장");
            Member member = newMember("멤버");
            Long roomId = newRoom(owner, member);

            Response link = get(owner, "/rooms/" + roomId + "/invite-link").ok();
            String code = link.read("$.data.inviteCode");
            assertThat(link.<String>read("$.data.inviteUrl")).endsWith("/invite/" + code);

            get(member, "/rooms/" + roomId + "/invite-link").fails(403, "ROOM_OWNER_ONLY");
        }

        @Test
        @DisplayName("미리보기 myStatus: NONE → PENDING, 멤버는 MEMBER")
        void previewStatus() throws Exception {
            Member owner = newMember("방장");
            Member joiner = newMember("참여자");
            Long roomId = newRoom(owner);
            String code = inviteCode(owner, roomId);

            Response before = get(joiner, "/invites/" + code).ok();
            assertThat(before.id("$.data.roomId")).isEqualTo(roomId);
            assertThat(before.<String>read("$.data.myStatus")).isEqualTo("NONE");

            requestJoin(joiner, code);
            assertThat(get(joiner, "/invites/" + code).ok().<String>read("$.data.myStatus")).isEqualTo("PENDING");
            assertThat(get(owner, "/invites/" + code).ok().<String>read("$.data.myStatus")).isEqualTo("MEMBER");
        }

        @Test
        @DisplayName("없는 초대 코드는 INVITE_CODE_NOT_FOUND, 비활성 방은 ROOM_INACTIVE")
        void invalidCode() throws Exception {
            Member owner = newMember("방장");
            Member joiner = newMember("참여자");
            Long roomId = newRoom(owner);
            String code = inviteCode(owner, roomId);

            get(joiner, "/invites/nothing").fails(404, "INVITE_CODE_NOT_FOUND");
            post(joiner, "/invites/nothing/join-requests").fails(404, "INVITE_CODE_NOT_FOUND");

            jdbc.update("UPDATE room SET status = 'INACTIVE' WHERE id = ?", roomId);
            get(joiner, "/invites/" + code).fails(403, "ROOM_INACTIVE");
        }

        @Test
        @DisplayName("대기 중 요청이 있으면 중복 요청 불가, 이미 멤버면 roomId 와 함께 JOIN_ALREADY_MEMBER")
        void duplicateRequests() throws Exception {
            Member owner = newMember("방장");
            Member joiner = newMember("참여자");
            Long roomId = newRoom(owner);
            String code = inviteCode(owner, roomId);

            requestJoin(joiner, code);
            post(joiner, "/invites/" + code + "/join-requests").fails(409, "JOIN_REQUEST_ALREADY_PENDING");

            Response already = post(owner, "/invites/" + code + "/join-requests").fails(409, "JOIN_ALREADY_MEMBER");
            assertThat(already.id("$.error.data.roomId")).isEqualTo(roomId);
        }

        @Test
        @DisplayName("방장이 승인하면 멤버가 되고, 최초 참여 프로필 설정 대상이 된다")
        void approve() throws Exception {
            Member owner = newMember("방장");
            Member joiner = newMember("참여자");
            Long roomId = newRoom(owner);
            Long requestId = requestJoin(joiner, inviteCode(owner, roomId));

            Response pending = get(owner, "/rooms/" + roomId + "/join-requests").ok();
            assertThat(pending.<Integer>read("$.data.totalElements")).isEqualTo(1);
            assertThat(pending.<String>read("$.data.content[0].requester.name")).isEqualTo("참여자");

            Response approved = post(owner, "/rooms/" + roomId + "/join-requests/" + requestId + "/approve").ok();
            assertThat(approved.<String>read("$.data.status")).isEqualTo("APPROVED");

            Response room = get(joiner, "/rooms/" + roomId).ok();
            assertThat(room.<String>read("$.data.myMembership.role")).isEqualTo("MEMBER");
            assertThat(room.<Boolean>read("$.data.needsProfileSetup")).isTrue();

            post(owner, "/rooms/" + roomId + "/join-requests/" + requestId + "/approve").fails(409, "JOIN_REQUEST_ALREADY_PROCESSED");
        }

        @Test
        @DisplayName("참여 요청 목록·승인은 방장만")
        void joinRequestsOwnerOnly() throws Exception {
            Member owner = newMember("방장");
            Member member = newMember("멤버");
            Member joiner = newMember("참여자");
            Long roomId = newRoom(owner, member);
            Long requestId = requestJoin(joiner, inviteCode(owner, roomId));

            get(member, "/rooms/" + roomId + "/join-requests").fails(403, "ROOM_OWNER_ONLY");
            post(member, "/rooms/" + roomId + "/join-requests/" + requestId + "/approve").fails(403, "ROOM_OWNER_ONLY");
        }

        @Test
        @DisplayName("다른 방의 요청 ID 로는 처리할 수 없다 (JOIN_REQUEST_NOT_FOUND)")
        void requestOfOtherRoom() throws Exception {
            Member owner = newMember("방장");
            Member joiner = newMember("참여자");
            Long roomA = newRoom(owner);
            Long roomB = newRoom(owner);
            Long requestToB = requestJoin(joiner, inviteCode(owner, roomB));

            post(owner, "/rooms/" + roomA + "/join-requests/" + requestToB + "/approve").fails(404, "JOIN_REQUEST_NOT_FOUND");
        }

        @Test
        @DisplayName("거절된 사용자는 같은 링크로 다시 요청할 수 있다")
        void rejectThenRequestAgain() throws Exception {
            Member owner = newMember("방장");
            Member joiner = newMember("참여자");
            Long roomId = newRoom(owner);
            String code = inviteCode(owner, roomId);
            Long requestId = requestJoin(joiner, code);

            Response rejected = post(owner, "/rooms/" + roomId + "/join-requests/" + requestId + "/reject").ok();
            assertThat(rejected.<String>read("$.data.status")).isEqualTo("REJECTED");
            get(joiner, "/rooms/" + roomId).fails(403, "ROOM_NOT_MEMBER");

            requestJoin(joiner, code);
        }

        @Test
        @DisplayName("20명이 차면 미리보기는 FULL, 참여 요청과 승인은 ROOM_MEMBER_LIMIT_EXCEEDED")
        void memberLimit() throws Exception {
            Member owner = newMember("방장");
            Member early = newMember("먼저 요청");
            Member late = newMember("늦게 온 사람");
            Long roomId = newRoom(owner);
            String code = inviteCode(owner, roomId);
            Long earlyRequest = requestJoin(early, code);

            Member[] others = new Member[19];
            for (int i = 0; i < others.length; i++) {
                others[i] = newMember("멤버" + i);
            }
            jdbc.update("DELETE FROM room_member WHERE room_id = ? AND role = 'MEMBER'", roomId);
            for (Member m : others) {
                jdbc.update("INSERT INTO room_member(room_id, account_id, role, status) VALUES (?, ?, 'MEMBER', 'ACTIVE')", roomId, m.id());
            }

            assertThat(get(late, "/invites/" + code).ok().<String>read("$.data.myStatus")).isEqualTo("FULL");
            post(late, "/invites/" + code + "/join-requests").fails(409, "ROOM_MEMBER_LIMIT_EXCEEDED");
            post(owner, "/rooms/" + roomId + "/join-requests/" + earlyRequest + "/approve").fails(409, "ROOM_MEMBER_LIMIT_EXCEEDED");
        }
    }

    // ---------- 멤버 ----------

    @Nested
    @DisplayName("멤버·방별 프로필")
    class Members {

        @Test
        @DisplayName("멤버 목록의 표시 이름은 닉네임, 없으면 기본 이름")
        void displayName() throws Exception {
            Member owner = newMember("방장이름");
            Member joiner = newMember("참여자이름");
            Long roomId = newRoom(owner);
            join(owner, roomId, joiner);

            patch(owner, "/rooms/" + roomId + "/members/me/profile", """
                    {"nickname":"팀장"}""").ok();

            Response members = get(joiner, "/rooms/" + roomId + "/members").ok();
            List<String> names = members.read("$.data[*].displayName");
            assertThat(names).containsExactlyInAnyOrder("팀장", "참여자이름");
        }

        @Test
        @DisplayName("빈 바디로 건너뛰어도 프로필 설정 완료로 기록한다")
        void skipProfileSetup() throws Exception {
            Member owner = newMember("방장");
            Member joiner = newMember("참여자");
            Long roomId = newRoom(owner);
            join(owner, roomId, joiner);

            patch(joiner, "/rooms/" + roomId + "/members/me/profile", "{}").ok();

            assertThat(get(joiner, "/rooms/" + roomId).ok().<Boolean>read("$.data.needsProfileSetup")).isFalse();
        }

        @Test
        @DisplayName("같은 방에서 쓰는 닉네임은 쓸 수 없고, 특수문자도 막는다")
        void nicknameRules() throws Exception {
            Member owner = newMember("방장");
            Member joiner = newMember("참여자");
            Long roomId = newRoom(owner);
            join(owner, roomId, joiner);
            patch(owner, "/rooms/" + roomId + "/members/me/profile", """
                    {"nickname":"개발자"}""").ok();

            patch(joiner, "/rooms/" + roomId + "/members/me/profile", """
                    {"nickname":"개발자"}""").fails(409, "MEMBER_NICKNAME_DUPLICATED");
            patch(joiner, "/rooms/" + roomId + "/members/me/profile", """
                    {"nickname":"개발자!"}""").fails(400, "COMMON_INVALID_INPUT");
        }

        @Test
        @DisplayName("프로필 이미지는 본인이 올린 PROFILE_IMAGE 파일만")
        void profileImageRules() throws Exception {
            Member owner = newMember("방장");
            Member joiner = newMember("참여자");
            Long roomId = newRoom(owner);
            join(owner, roomId, joiner);
            Long ownersImage = newFile(owner, roomId, "PROFILE_IMAGE");
            Long attachment = newFile(joiner, roomId, "CHAT_ATTACHMENT");
            Long myImage = newFile(joiner, roomId, "PROFILE_IMAGE");

            patch(joiner, "/rooms/" + roomId + "/members/me/profile", """
                    {"profileFileId":%d}""".formatted(ownersImage)).fails(403, "FILE_NOT_OWNER");
            patch(joiner, "/rooms/" + roomId + "/members/me/profile", """
                    {"profileFileId":%d}""".formatted(attachment)).fails(400, "FILE_PURPOSE_MISMATCH");

            Response ok = patch(joiner, "/rooms/" + roomId + "/members/me/profile", """
                    {"profileFileId":%d,"positionLabel":"백엔드"}""".formatted(myImage)).ok();
            assertThat(ok.<String>read("$.data.profileImageUrl")).isNotNull();
            assertThat(ok.<String>read("$.data.positionLabel")).isEqualTo("백엔드");
        }

        @Test
        @DisplayName("멤버 프로필: 다른 멤버는 1:1 대화 가능, 본인은 불가")
        void memberProfile() throws Exception {
            Member owner = newMember("방장");
            Member joiner = newMember("참여자");
            Long roomId = newRoom(owner);
            join(owner, roomId, joiner);

            Response other = get(owner, "/rooms/" + roomId + "/members/" + joiner.id()).ok();
            assertThat(other.<String>read("$.data.memberStatus")).isEqualTo("ACTIVE");
            assertThat(other.<String>read("$.data.displayName")).isEqualTo("참여자");
            assertThat(other.<Boolean>read("$.data.canStartDirectMessage")).isTrue();

            Response self = get(owner, "/rooms/" + roomId + "/members/" + owner.id()).ok();
            assertThat(self.<Boolean>read("$.data.canStartDirectMessage")).isFalse();

            get(owner, "/rooms/" + roomId + "/members/" + newMember("외부인").id()).fails(404, "MEMBER_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("나가기·내보내기·방장 위임")
    class Leave {

        @Test
        @DisplayName("방장은 나갈 수 없다 (leave-check 도 leavable=false)")
        void ownerCannotLeave() throws Exception {
            Member owner = newMember("방장");
            Long roomId = newRoom(owner);

            Response check = get(owner, "/rooms/" + roomId + "/members/me/leave-check").ok();
            assertThat(check.<Boolean>read("$.data.leavable")).isFalse();
            assertThat(check.<Boolean>read("$.data.isOwner")).isTrue();

            delete(owner, "/rooms/" + roomId + "/members/me").fails(409, "MEMBER_OWNER_CANNOT_LEAVE");
        }

        @Test
        @DisplayName("나간 멤버는 방에 접근할 수 없고, 프로필은 LEFT 로 이름·이미지 없이 보인다")
        void leave() throws Exception {
            Member owner = newMember("방장");
            Member joiner = newMember("참여자");
            Long roomId = newRoom(owner);
            join(owner, roomId, joiner);
            patch(joiner, "/rooms/" + roomId + "/members/me/profile", """
                    {"nickname":"떠날사람"}""").ok();

            assertThat(get(joiner, "/rooms/" + roomId + "/members/me/leave-check").ok().<Boolean>read("$.data.leavable")).isTrue();
            delete(joiner, "/rooms/" + roomId + "/members/me").ok();

            get(joiner, "/rooms/" + roomId).fails(403, "ROOM_NOT_MEMBER");
            Response profile = get(owner, "/rooms/" + roomId + "/members/" + joiner.id()).ok();
            assertThat(profile.<String>read("$.data.memberStatus")).isEqualTo("LEFT");
            assertThat(profile.<String>read("$.data.displayName")).isNull();
            assertThat(profile.<Boolean>read("$.data.canStartDirectMessage")).isFalse();
            List<Number> memberIds = get(owner, "/rooms/" + roomId + "/members").ok().read("$.data[*].accountId");
            assertThat(memberIds).extracting(Number::longValue).doesNotContain(joiner.id());
        }

        @Test
        @DisplayName("나갔다가 다시 승인받으면 같은 멤버로 복구되고 프로필 설정을 다시 묻는다")
        void rejoinAfterLeave() throws Exception {
            Member owner = newMember("방장");
            Member joiner = newMember("참여자");
            Long roomId = newRoom(owner);
            join(owner, roomId, joiner);
            patch(joiner, "/rooms/" + roomId + "/members/me/profile", "{}").ok();
            delete(joiner, "/rooms/" + roomId + "/members/me").ok();

            join(owner, roomId, joiner);

            Response room = get(joiner, "/rooms/" + roomId).ok();
            assertThat(room.<Boolean>read("$.data.needsProfileSetup")).isTrue();
            assertThat(room.<Integer>read("$.data.memberCount")).isEqualTo(2);
        }

        @Test
        @DisplayName("내보내기: 방장만, 본인은 불가, 내보내진 사람은 다시 참여할 수 없다")
        void kick() throws Exception {
            Member owner = newMember("방장");
            Member member = newMember("멤버");
            Member target = newMember("대상");
            Long roomId = newRoom(owner);
            join(owner, roomId, member);
            join(owner, roomId, target);
            String code = inviteCode(owner, roomId);

            delete(member, "/rooms/" + roomId + "/members/" + target.id()).fails(403, "ROOM_OWNER_ONLY");
            delete(owner, "/rooms/" + roomId + "/members/" + owner.id()).fails(400, "MEMBER_CANNOT_KICK_SELF");

            delete(owner, "/rooms/" + roomId + "/members/" + target.id()).ok();

            get(target, "/rooms/" + roomId).fails(403, "ROOM_NOT_MEMBER");
            assertThat(get(target, "/invites/" + code).ok().<String>read("$.data.myStatus")).isEqualTo("KICKED");
            post(target, "/invites/" + code + "/join-requests").fails(403, "JOIN_KICKED_MEMBER");
            delete(owner, "/rooms/" + roomId + "/members/" + target.id()).fails(404, "MEMBER_NOT_FOUND");
        }

        @Test
        @DisplayName("방장 위임: 역할이 바뀌고, 전 방장은 이제 나갈 수 있다")
        void delegate() throws Exception {
            Member owner = newMember("방장");
            Member next = newMember("다음 방장");
            Long roomId = newRoom(owner);
            join(owner, roomId, next);

            Response members = post(owner, "/rooms/" + roomId + "/delegate-owner", """
                    {"targetAccountId":%d}""".formatted(next.id())).ok();
            List<String> ownerRoles = members.read("$.data[?(@.accountId == %d)].role".formatted(next.id()));
            assertThat(ownerRoles).containsExactly("OWNER");

            assertThat(get(owner, "/rooms/" + roomId).ok().<String>read("$.data.myMembership.role")).isEqualTo("MEMBER");
            get(owner, "/rooms/" + roomId + "/invite-link").fails(403, "ROOM_OWNER_ONLY");
            delete(owner, "/rooms/" + roomId + "/members/me").ok();
        }

        @Test
        @DisplayName("위임 대상이 본인이거나 비활성 멤버면 MEMBER_DELEGATE_TARGET_INVALID")
        void delegateInvalidTarget() throws Exception {
            Member owner = newMember("방장");
            Member left = newMember("나간 멤버");
            Long roomId = newRoom(owner);
            join(owner, roomId, left);
            delete(left, "/rooms/" + roomId + "/members/me").ok();

            post(owner, "/rooms/" + roomId + "/delegate-owner", """
                    {"targetAccountId":%d}""".formatted(owner.id())).fails(400, "MEMBER_DELEGATE_TARGET_INVALID");
            post(owner, "/rooms/" + roomId + "/delegate-owner", """
                    {"targetAccountId":%d}""".formatted(left.id())).fails(400, "MEMBER_DELEGATE_TARGET_INVALID");
            post(owner, "/rooms/" + roomId + "/delegate-owner", """
                    {"targetAccountId":%d}""".formatted(newMember("외부인").id())).fails(404, "MEMBER_NOT_FOUND");
        }
    }
}
