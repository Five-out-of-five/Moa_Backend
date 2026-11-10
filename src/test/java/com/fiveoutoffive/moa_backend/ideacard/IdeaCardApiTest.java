package com.fiveoutoffive.moa_backend.ideacard;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("08. IdeaCard")
class IdeaCardApiTest extends Be2ApiTestSupport {

    Member owner;
    Member member;
    Long roomId;
    String cards;

    @BeforeEach
    void setUp() throws Exception {
        owner = newMember("방장");
        member = newMember("멤버");
        roomId = newRoom(owner, member);
        cards = "/rooms/" + roomId + "/idea-cards";
    }

    private Long startSession() throws Exception {
        return post(owner, "/rooms/" + roomId + "/sessions", "{\"title\":\"회차\"}").ok().id("$.data.sessionId");
    }

    private Long createCard(Member writer, String type, String title) throws Exception {
        return post(writer, cards, """
                {"cardType":"%s","title":"%s","content":"내용"}""".formatted(type, title)).ok().id("$.data.cardId");
    }

    @Test
    @DisplayName("진행 중 회차가 없으면 카드를 등록할 수 없다")
    void noSession() throws Exception {
        post(owner, cards, """
                {"cardType":"KEEP","title":"카드"}""").fails(409, "SESSION_NONE_IN_PROGRESS");
    }

    @Test
    @DisplayName("등록한 카드는 목록·상세에 나오고 진행 중 회차에 묶인다")
    void createListDetail() throws Exception {
        Long sessionId = startSession();
        Long cardId = createCard(member, "PROBLEM", "문제 카드");

        Response detail = get(owner, cards + "/" + cardId).ok();
        assertThat(detail.<String>read("$.data.title")).isEqualTo("문제 카드");
        assertThat(detail.id("$.data.sessionId")).isEqualTo(sessionId);
        assertThat(detail.id("$.data.writer.accountId")).isEqualTo(member.id());

        List<Integer> ids = get(owner, cards).ok().read("$.data[*].cardId");
        assertThat(ids).contains(cardId.intValue());
    }

    @Test
    @DisplayName("본인 카드만 수정·삭제할 수 있다")
    void writerOnly() throws Exception {
        startSession();
        Long cardId = createCard(member, "KEEP", "멤버 카드");

        patch(owner, cards + "/" + cardId, "{\"title\":\"남이 수정\"}").fails(403, "CARD_NOT_WRITER");
        delete(owner, cards + "/" + cardId).fails(403, "CARD_NOT_WRITER");

        Response updated = patch(member, cards + "/" + cardId, "{\"title\":\"본인 수정\"}").ok();
        assertThat(updated.<String>read("$.data.title")).isEqualTo("본인 수정");
        assertThat(updated.<String>read("$.data.content")).isEqualTo("내용");
    }

    @Test
    @DisplayName("삭제한 카드는 상세·목록에서 사라진다")
    void deleteCard() throws Exception {
        startSession();
        Long cardId = createCard(owner, "KEEP", "지울 카드");

        delete(owner, cards + "/" + cardId).ok();

        get(owner, cards + "/" + cardId).fails(404, "CARD_NOT_FOUND");
        List<Integer> ids = get(owner, cards).ok().read("$.data[*].cardId");
        assertThat(ids).doesNotContain(cardId.intValue());
    }

    @Test
    @DisplayName("다른 방의 카드는 CARD_NOT_FOUND")
    void otherRoomCard() throws Exception {
        Member stranger = newMember("다른방장");
        Long otherRoom = newRoom(stranger);
        post(stranger, "/rooms/" + otherRoom + "/sessions", "{\"title\":\"회차\"}").ok();
        Long otherCard = post(stranger, "/rooms/" + otherRoom + "/idea-cards", """
                {"cardType":"KEEP","title":"남의 카드"}""").ok().id("$.data.cardId");

        get(owner, cards + "/" + otherCard).fails(404, "CARD_NOT_FOUND");
    }

    @Test
    @DisplayName("편집 잠금은 작성자가 걸고 풀 수 있다")
    void editLock() throws Exception {
        startSession();
        Long cardId = createCard(owner, "KEEP", "잠글 카드");

        Response locked = post(owner, cards + "/" + cardId + "/edit-lock").ok();
        assertThat(locked.id("$.data.lockedBy.accountId")).isEqualTo(owner.id());
        assertThat(locked.<Object>read("$.data.expiresAt")).isNotNull();

        post(member, cards + "/" + cardId + "/edit-lock").fails(403, "CARD_NOT_WRITER");
        delete(owner, cards + "/" + cardId + "/edit-lock").ok();
    }

    @Test
    @DisplayName("카드 이동은 version 이 맞을 때만, 다른 칸으로 옮기면 cardType 이 바뀐다")
    void move() throws Exception {
        startSession();
        Long cardId = createCard(owner, "KEEP", "옮길 카드");
        long version = get(owner, cards + "/" + cardId).ok().id("$.data.version");

        patch(owner, cards + "/" + cardId + "/position", """
                {"cardType":"TRY","toIndex":0,"version":%d}""".formatted(version + 100)).fails(409, "CARD_VERSION_CONFLICT");

        patch(member, cards + "/" + cardId + "/position", """
                {"cardType":"TRY","toIndex":0,"version":%d}""".formatted(version)).ok();
        Response detail = get(owner, cards + "/" + cardId).ok();
        assertThat(detail.<String>read("$.data.cardType")).isEqualTo("TRY");
        assertThat(detail.id("$.data.version")).isGreaterThan(version);
    }

    @Test
    @DisplayName("리액션은 멱등이고 누른 사람 목록에 나오며 취소할 수 있다")
    void reactions() throws Exception {
        startSession();
        Long cardId = createCard(owner, "KEEP", "리액션 카드");
        String path = cards + "/" + cardId + "/reactions";

        put(member, path + "/THUMBS_UP", null).ok();
        Response again = put(member, path + "/THUMBS_UP", null).ok();
        List<Integer> counts = again.read("$.data[?(@.emoji=='THUMBS_UP')].count");
        assertThat(counts).containsExactly(1);

        List<Integer> users = get(owner, path).ok().read("$.data[?(@.emoji=='THUMBS_UP')].users[*].accountId");
        assertThat(users).containsExactly(member.id().intValue());

        delete(member, path + "/THUMBS_UP").ok();
        List<Object> left = get(owner, path).ok().read("$.data[?(@.emoji=='THUMBS_UP')]");
        assertThat(left).isEmpty();
    }

    @Test
    @DisplayName("정해진 이모지(THUMBS_UP/CLAP/FIRE/THINKING/SAD)만 남길 수 있다")
    void unknownEmoji() throws Exception {
        startSession();
        Long cardId = createCard(owner, "KEEP", "카드");

        put(member, cards + "/" + cardId + "/reactions/HEART", null).fails(400, "COMMON_INVALID_INPUT");
    }

    @Test
    @DisplayName("댓글 작성·수정·삭제, 남의 댓글은 고칠 수 없다")
    void comments() throws Exception {
        startSession();
        Long cardId = createCard(owner, "KEEP", "댓글 카드");
        String path = cards + "/" + cardId + "/comments";

        Long commentId = post(member, path, "{\"content\":\"첫 댓글\"}").ok().id("$.data.commentId");
        patch(owner, path + "/" + commentId, "{\"content\":\"남이 수정\"}").fails(403, "COMMENT_NOT_WRITER");
        delete(owner, path + "/" + commentId).fails(403, "COMMENT_NOT_WRITER");

        Response edited = patch(member, path + "/" + commentId, "{\"content\":\"고친 댓글\"}").ok();
        assertThat(edited.<String>read("$.data.content")).isEqualTo("고친 댓글");
        assertThat(edited.<Boolean>read("$.data.edited")).isTrue();

        delete(member, path + "/" + commentId).ok();
        List<Boolean> deleted = get(owner, path).ok().read("$.data[?(@.commentId==" + commentId + ")].deleted");
        assertThat(deleted).allMatch(Boolean.TRUE::equals);
        patch(member, path + "/" + commentId, "{\"content\":\"삭제 후 수정\"}").fails(404, "COMMENT_NOT_FOUND");
    }

    @Test
    @DisplayName("없는 파일은 첨부할 수 없다")
    void attachUnknownFile() throws Exception {
        startSession();
        Long cardId = createCard(owner, "KEEP", "첨부 카드");

        post(owner, cards + "/" + cardId + "/files", "{\"fileId\":999999999}").fails(404, "FILE_NOT_FOUND");
    }

    /** 업로드(BE-1 파일 API)는 S3 가 필요해서, 업로드가 끝난 상태의 file 행을 직접 넣는다. 이미지가 아니면 미리보기 URL 을 만들지 않는다. */
    private Long uploadedFile(Member uploader, String group) {
        return jdbc.queryForObject("""
                INSERT INTO file(owner_account_id, room_id, file_group, object_key, original_name, mime_type, size_bytes)
                VALUES (?, ?, ?, ?, 'memo.txt', 'text/plain', 10) RETURNING id
                """, Long.class, uploader.id(), roomId, group, "test/" + java.util.UUID.randomUUID());
    }

    @Test
    @DisplayName("파일 첨부: 등록할 때 첨부, 나중에 추가, 첨부 삭제")
    void attachFlow() throws Exception {
        startSession();
        Long first = uploadedFile(owner, "CARD_ATTACHMENT");
        Long cardId = post(owner, cards, """
                {"cardType":"KEEP","title":"첨부 카드","fileIds":[%d]}""".formatted(first)).ok().id("$.data.cardId");
        Long second = uploadedFile(owner, "CARD_ATTACHMENT");

        Response attached = post(owner, cards + "/" + cardId + "/files", "{\"fileId\":%d}".formatted(second)).ok();
        List<Integer> ids = attached.read("$.data[*].fileId");
        assertThat(ids).containsExactly(first.intValue(), second.intValue());
        assertThat(get(owner, cards + "/" + cardId).ok().<Integer>read("$.data.fileCount")).isEqualTo(2);

        delete(owner, cards + "/" + cardId + "/files/" + first).ok();
        List<Integer> left = get(owner, cards + "/" + cardId).ok().read("$.data.files[*].fileId");
        assertThat(left).containsExactly(second.intValue());
        assertThat(jdbc.queryForObject("SELECT deleted_at IS NOT NULL FROM file WHERE id = ?", Boolean.class, first)).isTrue();
    }

    @Test
    @DisplayName("파일 첨부 거절: 남의 파일, 용도가 다른 파일, 이미 붙은 파일, 5개 초과, 같은 파일 중복")
    void attachRules() throws Exception {
        startSession();
        Long cardId = createCard(owner, "KEEP", "첨부 카드");
        String files = cards + "/" + cardId + "/files";

        post(owner, files, "{\"fileId\":%d}".formatted(uploadedFile(member, "CARD_ATTACHMENT"))).fails(403, "FILE_NOT_OWNER");
        post(owner, files, "{\"fileId\":%d}".formatted(uploadedFile(owner, "CHAT_ATTACHMENT"))).fails(400, "FILE_PURPOSE_MISMATCH");

        Long used = uploadedFile(owner, "CARD_ATTACHMENT");
        post(owner, files, "{\"fileId\":%d}".formatted(used)).ok();
        Long otherCard = createCard(owner, "TRY", "다른 카드");
        post(owner, cards + "/" + otherCard + "/files", "{\"fileId\":%d}".formatted(used)).fails(409, "FILE_ALREADY_ATTACHED");

        for (int i = 0; i < 4; i++) {
            post(owner, files, "{\"fileId\":%d}".formatted(uploadedFile(owner, "CARD_ATTACHMENT"))).ok();
        }
        post(owner, files, "{\"fileId\":%d}".formatted(uploadedFile(owner, "CARD_ATTACHMENT"))).fails(409, "FILE_ATTACHMENT_LIMIT_EXCEEDED");

        Long dup = uploadedFile(owner, "CARD_ATTACHMENT");
        post(owner, cards, """
                {"cardType":"KEEP","title":"중복 첨부","fileIds":[%d,%d]}""".formatted(dup, dup)).fails(400, "COMMON_INVALID_INPUT");
    }

    @Test
    @DisplayName("파일 첨부·삭제는 카드 작성자만")
    void attachWriterOnly() throws Exception {
        startSession();
        Long fileId = uploadedFile(owner, "CARD_ATTACHMENT");
        Long cardId = post(owner, cards, """
                {"cardType":"KEEP","title":"첨부 카드","fileIds":[%d]}""".formatted(fileId)).ok().id("$.data.cardId");

        post(member, cards + "/" + cardId + "/files", "{\"fileId\":%d}".formatted(uploadedFile(member, "CARD_ATTACHMENT")))
                .fails(403, "CARD_NOT_WRITER");
        delete(member, cards + "/" + cardId + "/files/" + fileId).fails(403, "CARD_NOT_WRITER");
    }

    @Test
    @DisplayName("지난 회차 카드 조회: 없는 회차·다른 방 회차는 SESSION_NOT_FOUND")
    void listUnknownSession() throws Exception {
        Member stranger = newMember("다른방장");
        Long otherRoom = newRoom(stranger);
        Long otherSession = post(stranger, "/rooms/" + otherRoom + "/sessions", "{\"title\":\"남의 회차\"}").ok().id("$.data.sessionId");

        get(owner, cards + "?sessionId=999999999").fails(404, "SESSION_NOT_FOUND");
        get(owner, cards + "?sessionId=" + otherSession).fails(404, "SESSION_NOT_FOUND");
        assertThat(get(owner, cards).ok().<List<Object>>read("$.data")).isEmpty();
    }

    @Test
    @DisplayName("삭제된 카드에 대한 요청은 모두 CARD_NOT_FOUND")
    void deletedCardEverywhere() throws Exception {
        startSession();
        Long cardId = createCard(owner, "KEEP", "지울 카드");
        delete(owner, cards + "/" + cardId).ok();
        String card = cards + "/" + cardId;

        patch(owner, card, "{\"title\":\"수정\"}").fails(404, "CARD_NOT_FOUND");
        delete(owner, card).fails(404, "CARD_NOT_FOUND");
        patch(owner, card + "/position", "{\"cardType\":\"TRY\",\"toIndex\":0,\"version\":0}").fails(404, "CARD_NOT_FOUND");
        put(member, card + "/reactions/CLAP", null).fails(404, "CARD_NOT_FOUND");
        get(member, card + "/reactions").fails(404, "CARD_NOT_FOUND");
        delete(member, card + "/reactions/CLAP").fails(404, "CARD_NOT_FOUND");
        get(member, card + "/comments").fails(404, "CARD_NOT_FOUND");
        post(member, card + "/comments", "{\"content\":\"댓글\"}").fails(404, "CARD_NOT_FOUND");
        post(owner, card + "/files", "{\"fileId\":%d}".formatted(uploadedFile(owner, "CARD_ATTACHMENT"))).fails(404, "CARD_NOT_FOUND");
        delete(owner, card + "/files/1").fails(404, "CARD_NOT_FOUND");
    }

    @Test
    @DisplayName("요청 값 검사: 제목 누락·100자 초과, 댓글 500자 초과")
    void validation() throws Exception {
        startSession();
        post(owner, cards, "{\"cardType\":\"KEEP\",\"title\":\"\"}").fails(400, "COMMON_INVALID_INPUT");
        post(owner, cards, "{\"cardType\":\"KEEP\",\"title\":\"%s\"}".formatted("가".repeat(101))).fails(400, "COMMON_INVALID_INPUT");
        post(owner, cards, "{\"title\":\"종류 없음\"}").fails(400, "COMMON_INVALID_INPUT");
        Long cardId = createCard(owner, "KEEP", "카드");
        post(member, cards + "/" + cardId + "/comments", "{\"content\":\"%s\"}".formatted("가".repeat(501))).fails(400, "COMMON_INVALID_INPUT");
    }

    @Nested
    @DisplayName("마감된 회차의 카드")
    class ClosedSession {

        Long cardId;
        Long sessionId;
        Long fileId;

        @BeforeEach
        void close() throws Exception {
            sessionId = startSession();
            fileId = uploadedFile(owner, "CARD_ATTACHMENT");
            cardId = post(owner, cards, """
                    {"cardType":"KEEP","title":"마감될 카드","fileIds":[%d]}""".formatted(fileId)).ok().id("$.data.cardId");
            put(member, cards + "/" + cardId + "/reactions/CLAP", null).ok();
            post(member, cards + "/" + cardId + "/comments", "{\"content\":\"마감 전 댓글\"}").ok();
            post(owner, "/rooms/" + roomId + "/sessions/" + sessionId + "/close").ok();
        }

        @Test
        @DisplayName("수정·삭제·잠금 불가")
        void readOnly() throws Exception {
            patch(owner, cards + "/" + cardId, "{\"title\":\"마감 후 수정\"}").fails(409, "CARD_SESSION_CLOSED");
            delete(owner, cards + "/" + cardId).fails(409, "CARD_SESSION_CLOSED");
            post(owner, cards + "/" + cardId + "/edit-lock").fails(409, "CARD_SESSION_CLOSED");
        }

        @Test
        @DisplayName("새 댓글·리액션 불가, 기존 리액션도 취소 불가")
        void noNewActivity() throws Exception {
            post(member, cards + "/" + cardId + "/comments", "{\"content\":\"마감 후 댓글\"}").fails(409, "CARD_SESSION_CLOSED");
            put(member, cards + "/" + cardId + "/reactions/FIRE", null).fails(409, "CARD_SESSION_CLOSED");
            delete(member, cards + "/" + cardId + "/reactions/CLAP").fails(409, "CARD_SESSION_CLOSED");
        }

        @Test
        @DisplayName("기존 댓글 수정·삭제, 파일 첨부·첨부 삭제 불가")
        void noCommentEditOrAttach() throws Exception {
            Long commentId = get(member, cards + "/" + cardId + "/comments").ok().first("$.data[*].commentId");

            patch(member, cards + "/" + cardId + "/comments/" + commentId, "{\"content\":\"수정\"}").fails(409, "CARD_SESSION_CLOSED");
            delete(member, cards + "/" + cardId + "/comments/" + commentId).fails(409, "CARD_SESSION_CLOSED");
            post(owner, cards + "/" + cardId + "/files", "{\"fileId\":%d}".formatted(uploadedFile(owner, "CARD_ATTACHMENT")))
                    .fails(409, "CARD_SESSION_CLOSED");

            // 요구사항 27 "마감된 회차의 카드는 수정·삭제 불가". API 명세 첨부 삭제 에러 목록에는 아직 없음(추가 제안)
            delete(owner, cards + "/" + cardId + "/files/" + fileId).fails(409, "CARD_SESSION_CLOSED");
            List<Integer> files = get(owner, cards + "/" + cardId).ok().read("$.data.files[*].fileId");
            assertThat(files).containsExactly(fileId.intValue());
        }

        @Test
        @DisplayName("이동은 가능하다(명세: 마감 회차 카드도 이동 가능, 이동 API 에러에 CARD_SESSION_CLOSED 없음)")
        void moveAllowed() throws Exception {
            long version = get(owner, cards + "/" + cardId).ok().id("$.data.version");

            patch(member, cards + "/" + cardId + "/position", """
                    {"cardType":"PROBLEM","toIndex":0,"version":%d}""".formatted(version)).ok();
        }

        @Test
        @DisplayName("sessionId 로 지난 회차 카드를 조회할 수 있고 댓글·리액션은 보존된다")
        void history() throws Exception {
            List<Integer> ids = get(owner, cards + "?sessionId=" + sessionId).ok().read("$.data[*].cardId");
            assertThat(ids).containsExactly(cardId.intValue());

            Response detail = get(owner, cards + "/" + cardId).ok();
            assertThat(detail.<Integer>read("$.data.commentCount")).isEqualTo(1);
            List<Integer> clap = get(owner, cards + "/" + cardId + "/reactions").ok().read("$.data[?(@.emoji=='CLAP')].count");
            assertThat(clap).containsExactly(1);
        }
    }
}
