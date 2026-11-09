package com.fiveoutoffive.moa_backend.chat.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅 메시지 (chat_message)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    /** PK */
    private Long id;

    /** 채널 (NOT NULL) */
    private Long channelId;

    /** 보낸 사람 (SYSTEM 메시지는 NULL) */
    private Long senderAccountId;

    /** 메시지 종류 (NOT NULL) */
    private MessageType messageType;

    /** 내용 (파일만 보낸 경우 NULL) */
    private String content;

    /** 수정 시각 ('수정됨' 표시) */
    private LocalDateTime editedAt;

    /** 소프트 딜리트 시각 (NULL = 정상) */
    private LocalDateTime deletedAt;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    private ChatMessage(
            Long id,
            Long channelId,
            Long senderAccountId,
            MessageType messageType,
            String content,
            LocalDateTime editedAt,
            LocalDateTime deletedAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.channelId = channelId;
        this.senderAccountId = senderAccountId;
        this.messageType = messageType;
        this.content = content;
        this.editedAt = editedAt;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static ChatMessage reconstitute(
            Long id,
            Long channelId,
            Long senderAccountId,
            MessageType messageType,
            String content,
            LocalDateTime editedAt,
            LocalDateTime deletedAt,
            LocalDateTime createdAt
    ) {
        return new ChatMessage(id, channelId, senderAccountId, messageType, content, editedAt, deletedAt, createdAt);
    }
}
