package com.fiveoutoffive.moa_backend.file;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 03. File (API 5개). 요구사항 55~58.
 *
 * <p>실제 업로드까지 확인하므로 로컬 MinIO(docker compose 의 minio, 버킷 moa-local)가 떠 있어야 한다.
 */
@DisplayName("03. File")
class FileApiTest extends Be2ApiTestSupport {

    private Response upload(Member who, Long roomId, String purpose, String filename, String mime, byte[] content) throws Exception {
        var req = MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/v1/rooms/" + roomId + "/files")
                .file(new MockMultipartFile("file", filename, mime, content))
                .param("purpose", purpose)
                .header("Authorization", "Bearer " + who.token());
        var res = mockMvc.perform(req).andReturn().getResponse();
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return new Response(res.getStatus(), res.getContentAsString());
    }

    private Long uploadText(Member who, Long roomId, String purpose) throws Exception {
        return upload(who, roomId, purpose, "memo.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)).ok().id("$.data.fileId");
    }

    private Long cardWithFile(Member writer, Long roomId, Long fileId) throws Exception {
        post(writer, "/rooms/" + roomId + "/sessions", """
                {"title":"1회차"}""").ok();
        return post(writer, "/rooms/" + roomId + "/idea-cards", """
                {"cardType":"KEEP","title":"첨부 카드","fileIds":[%d]}""".formatted(fileId)).ok().id("$.data.cardId");
    }

    @Test
    @DisplayName("첨부를 올리면 아직 어디에도 연결되지 않은 fileId 를 받는다. 이미지 용도는 미리보기 주소가 있다")
    void upload() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);

        Response text = upload(owner, roomId, "CARD_ATTACHMENT", "memo.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)).ok();
        assertThat(text.<String>read("$.data.purpose")).isEqualTo("CARD_ATTACHMENT");
        assertThat(text.<Integer>read("$.data.sizeBytes")).isEqualTo(5);
        assertThat(text.<String>read("$.data.previewUrl")).isNull();

        Response image = upload(owner, roomId, "PROFILE_IMAGE", "me.png", "image/png", new byte[]{1, 2, 3}).ok();
        assertThat(image.<String>read("$.data.previewUrl")).isNotNull();
    }

    @Test
    @DisplayName("허용되지 않은 형식(실행 파일, 프로필에 gif)은 FILE_TYPE_NOT_ALLOWED")
    void typeNotAllowed() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);

        upload(owner, roomId, "CHAT_ATTACHMENT", "virus.exe", "application/octet-stream", new byte[]{1}).fails(400, "FILE_TYPE_NOT_ALLOWED");
        upload(owner, roomId, "PROFILE_IMAGE", "anim.gif", "image/gif", new byte[]{1}).fails(400, "FILE_TYPE_NOT_ALLOWED");
    }

    @Test
    @DisplayName("이미지 용도는 5MB 를 넘으면 FILE_SIZE_EXCEEDED (413)")
    void imageSizeLimit() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);

        upload(owner, roomId, "PROFILE_IMAGE", "big.png", "image/png", new byte[5 * 1024 * 1024 + 1]).fails(413, "FILE_SIZE_EXCEEDED");
    }

    @Test
    @DisplayName("방 저장 용량 2GB 를 넘으면 FILE_STORAGE_LIMIT_EXCEEDED")
    void roomStorageLimit() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        jdbc.update("""
                INSERT INTO file(owner_account_id, room_id, file_group, object_key, original_name, mime_type, size_bytes)
                VALUES (?, ?, 'CHAT_ATTACHMENT', ?, 'huge.zip', 'application/zip', ?)
                """, owner.id(), roomId, "test/" + java.util.UUID.randomUUID(), 2L * 1024 * 1024 * 1024);

        upload(owner, roomId, "CHAT_ATTACHMENT", "memo.txt", "text/plain", new byte[]{1}).fails(409, "FILE_STORAGE_LIMIT_EXCEEDED");
    }

    @Test
    @DisplayName("방 대표 이미지는 방장만, 방 멤버가 아니면 업로드 불가")
    void uploadPermissions() throws Exception {
        Member owner = newMember("방장");
        Member member = newMember("멤버");
        Long roomId = newRoom(owner, member);

        upload(member, roomId, "ROOM_IMAGE", "room.png", "image/png", new byte[]{1}).fails(403, "ROOM_OWNER_ONLY");
        upload(newMember("외부인"), roomId, "CHAT_ATTACHMENT", "memo.txt", "text/plain", new byte[]{1}).fails(403, "ROOM_NOT_MEMBER");
    }

    @Test
    @DisplayName("업로드 취소: 본인 파일만, 이미 연결된 파일은 불가, 취소하면 내려받을 수 없다")
    void cancelUpload() throws Exception {
        Member owner = newMember("방장");
        Member member = newMember("멤버");
        Long roomId = newRoom(owner, member);
        Long mine = uploadText(owner, roomId, "CARD_ATTACHMENT");
        Long attached = uploadText(owner, roomId, "CARD_ATTACHMENT");
        cardWithFile(owner, roomId, attached);

        delete(member, "/rooms/" + roomId + "/files/" + mine).fails(403, "FILE_NOT_OWNER");
        delete(owner, "/rooms/" + roomId + "/files/" + attached).fails(409, "FILE_ALREADY_ATTACHED");

        delete(owner, "/rooms/" + roomId + "/files/" + mine).ok();
        get(owner, "/rooms/" + roomId + "/files/" + mine + "/download-url").fails(404, "FILE_NOT_FOUND");
    }

    @Test
    @DisplayName("방 파일 목록에는 연결된 첨부만 출처와 함께 나오고, 원본 카드가 삭제되면 빠진다")
    void roomFiles() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        Long attached = uploadText(owner, roomId, "CARD_ATTACHMENT");
        Long cardId = cardWithFile(owner, roomId, attached);
        upload(owner, roomId, "PROFILE_IMAGE", "me.png", "image/png", new byte[]{1}).ok();

        Response list = get(owner, "/rooms/" + roomId + "/files").ok();
        List<Number> ids = list.read("$.data.content[*].fileId");
        assertThat(ids).extracting(Number::longValue).containsExactly(attached);
        assertThat(list.<String>read("$.data.content[0].source")).isEqualTo("IDEA_CARD");
        assertThat(list.id("$.data.content[0].sourceId")).isEqualTo(cardId);
        assertThat(list.<String>read("$.data.content[0].uploader.displayName")).isEqualTo("방장");

        delete(owner, "/rooms/" + roomId + "/idea-cards/" + cardId).ok();

        assertThat(get(owner, "/rooms/" + roomId + "/files").ok().<Integer>read("$.data.totalElements")).isZero();
        get(owner, "/rooms/" + roomId + "/files/" + attached + "/download-url").fails(404, "FILE_NOT_FOUND");
    }

    @Test
    @DisplayName("다운로드 URL 은 짧은 유효기간의 서명 URL, 다른 방 파일 ID 는 FILE_NOT_FOUND")
    void downloadUrl() throws Exception {
        Member owner = newMember("방장");
        Long roomA = newRoom(owner);
        Long roomB = newRoom(owner);
        Long fileId = uploadText(owner, roomA, "CHAT_ATTACHMENT");

        Response res = get(owner, "/rooms/" + roomA + "/files/" + fileId + "/download-url").ok();
        assertThat(res.<String>read("$.data.url")).contains("X-Amz-Signature");
        assertThat(res.<String>read("$.data.expiresAt")).contains("+09:00");

        get(owner, "/rooms/" + roomB + "/files/" + fileId + "/download-url").fails(404, "FILE_NOT_FOUND");
    }

    @Test
    @DisplayName("저장 용량은 올린 파일 크기의 합, 한도는 2GB")
    void storageUsage() throws Exception {
        Member owner = newMember("방장");
        Long roomId = newRoom(owner);
        upload(owner, roomId, "CHAT_ATTACHMENT", "a.txt", "text/plain", new byte[100]).ok();
        upload(owner, roomId, "CHAT_ATTACHMENT", "b.txt", "text/plain", new byte[50]).ok();

        Response res = get(owner, "/rooms/" + roomId + "/storage-usage").ok();

        assertThat(res.<Integer>read("$.data.usedBytes")).isEqualTo(150);
        assertThat(res.<Long>read("$.data.limitBytes")).isEqualTo(2L * 1024 * 1024 * 1024);
    }
}
