package com.fiveoutoffive.moa_backend.chat.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅 채널 (공개 / 비공개 / 1:1) (chat_channel)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatChannel {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 공개 / 비공개 / 1:1 대화 (NOT NULL) */
    private ChannelType channelType;

    /** 채널 이름 (1:1 은 NULL) */
    private String name;

    /** 기본 채널(전체) 여부 — 나가기·삭제 불가 (NOT NULL) */
    private Boolean isDefault;

    /** 1:1 대화 식별 키 ('작은계정ID:큰계정ID') */
    private String dmKey;

    /** 채널 생성자 */
    private Long creatorAccountId;

    /** 소프트 딜리트 시각 (NULL = 정상) */
    private LocalDateTime deletedAt;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private ChatChannel(
            Long id,
            Long roomId,
            ChannelType channelType,
            String name,
            Boolean isDefault,
            String dmKey,
            Long creatorAccountId,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.channelType = channelType;
        this.name = name;
        this.isDefault = isDefault;
        this.dmKey = dmKey;
        this.creatorAccountId = creatorAccountId;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static ChatChannel reconstitute(
            Long id,
            Long roomId,
            ChannelType channelType,
            String name,
            Boolean isDefault,
            String dmKey,
            Long creatorAccountId,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new ChatChannel(id, roomId, channelType, name, isDefault, dmKey, creatorAccountId, deletedAt, createdAt, updatedAt);
    }
}
