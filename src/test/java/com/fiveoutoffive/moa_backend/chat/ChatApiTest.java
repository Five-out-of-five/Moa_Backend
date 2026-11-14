package com.fiveoutoffive.moa_backend.chat;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiveoutoffive.moa_backend.support.Be2ApiTestSupport;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/** 채팅 API. 채널 삭제 시 메시지와 첨부가 함께 지워지는지 확인한다. (요구사항 51) */
class ChatApiTest extends Be2ApiTestSupport {

    @Test
    void 채널을_삭제하면_메시지와_첨부도_삭제되고_방_파일_목록에서_빠진다() throws Exception {
        Member owner = newMember("채널방장");
        Long roomId = newRoom(owner);
        String channels = "/rooms/" + roomId + "/channels";
        Long channelId = post(owner, channels, "{\"name\":\"삭제할채널\",\"channelType\":\"PUBLIC\"}").ok().id("$.data.channelId");
        Long fileId = uploadChatFile(owner, roomId);
        Long messageId = post(owner, channels + "/" + channelId + "/messages",
                "{\"content\":\"첨부 있음\",\"fileIds\":[" + fileId + "],\"clientMessageId\":\"" + UUID.randomUUID() + "\"}").ok().id("$.data.messageId");
        assertThat(get(owner, "/rooms/" + roomId + "/files").ok().<Integer>read("$.data.totalElements")).isEqualTo(1);

        send(owner, HttpMethod.DELETE, channels + "/" + channelId, "{\"confirmName\":\"삭제할채널\"}").ok();

        assertThat(jdbc.queryForObject("SELECT deleted_at IS NOT NULL FROM chat_message WHERE id = ?", Boolean.class, messageId)).isTrue();
        assertThat(jdbc.queryForObject("SELECT deleted_at IS NOT NULL FROM file WHERE id = ?", Boolean.class, fileId)).isTrue();
        assertThat(get(owner, "/rooms/" + roomId + "/files").ok().<Integer>read("$.data.totalElements")).isZero();
        get(owner, "/rooms/" + roomId + "/files/" + fileId + "/download-url").fails(404, "FILE_NOT_FOUND");
    }

    @Test
    void 메시지가_없는_채널도_삭제된다() throws Exception {
        Member owner = newMember("채널방장");
        Long roomId = newRoom(owner);
        String channels = "/rooms/" + roomId + "/channels";
        Long channelId = post(owner, channels, "{\"name\":\"빈채널\",\"channelType\":\"PRIVATE\"}").ok().id("$.data.channelId");

        send(owner, HttpMethod.DELETE, channels + "/" + channelId, "{\"confirmName\":\"빈채널\"}").ok();

        get(owner, channels + "/" + channelId).fails(404, "CHANNEL_NOT_FOUND");
    }

    @Test
    void 공개_채널에서_나가면_myStatus_는_NONE() throws Exception {
        Member owner = newMember("채널방장");
        Member member = newMember("나가는멤버");
        Long roomId = newRoom(owner, member);
        String channels = "/rooms/" + roomId + "/channels";
        Long channelId = post(owner, channels, "{\"name\":\"공개\",\"channelType\":\"PUBLIC\"}").ok().id("$.data.channelId");
        post(member, channels + "/" + channelId + "/join").ok();

        post(member, channels + "/" + channelId + "/leave").ok();

        List<String> status = get(member, channels).ok().read("$.data[?(@.channelId == " + channelId + ")].myStatus");
        assertThat(status).containsExactly("NONE");
    }

    @Test
    void 같은_clientMessageId_로_다시_보내면_같은_메시지를_돌려준다() throws Exception {
        Member owner = newMember("채널방장");
        Long roomId = newRoom(owner);
        String channels = "/rooms/" + roomId + "/channels";
        Long channelId = post(owner, channels, "{\"name\":\"재전송\",\"channelType\":\"PUBLIC\"}").ok().id("$.data.channelId");
        String body = "{\"content\":\"한 번만\",\"clientMessageId\":\"retry-1\"}";

        Response first = post(owner, channels + "/" + channelId + "/messages", body).ok();
        Response second = post(owner, channels + "/" + channelId + "/messages", body).ok();

        assertThat(second.id("$.data.messageId")).isEqualTo(first.id("$.data.messageId"));
        assertThat(second.<String>read("$.data.clientMessageId")).isEqualTo("retry-1");
    }

    @Test
    void 메시지_첨부가_5개를_넘으면_명세_코드로_거절한다() throws Exception {
        Member owner = newMember("채널방장");
        Long roomId = newRoom(owner);
        String channels = "/rooms/" + roomId + "/channels";
        Long channelId = post(owner, channels, "{\"name\":\"첨부\",\"channelType\":\"PUBLIC\"}").ok().id("$.data.channelId");

        post(owner, channels + "/" + channelId + "/messages", "{\"fileIds\":[1,2,3,4,5,6],\"clientMessageId\":\"" + UUID.randomUUID() + "\"}")
                .fails(409, "FILE_ATTACHMENT_LIMIT_EXCEEDED");
    }

    private Long uploadChatFile(Member who, Long roomId) throws Exception {
        var req = MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/v1/rooms/" + roomId + "/files")
                .file(new MockMultipartFile("file", "memo.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)))
                .param("purpose", "CHAT_ATTACHMENT")
                .header("Authorization", "Bearer " + who.token());
        var res = mockMvc.perform(req).andReturn().getResponse();
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return new Response(res.getStatus(), res.getContentAsString()).ok().id("$.data.fileId");
    }
}
