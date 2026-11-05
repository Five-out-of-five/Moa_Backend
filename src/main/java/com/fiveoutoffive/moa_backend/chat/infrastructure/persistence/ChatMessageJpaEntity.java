package com.fiveoutoffive.moa_backend.chat.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.chat.domain.model.MessageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 채팅 메시지 (chat_message). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(name = "sender_account_id")
    private Long senderAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType;

    @Column(name = "content", length = 2000)
    private String content;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public ChatMessageJpaEntity(
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

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.messageType == null) {
            this.messageType = MessageType.TEXT;
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }
}
