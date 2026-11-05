package com.fiveoutoffive.moa_backend.chat.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채널 참여자 + 읽음 위치 (chat_channel_member)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatChannelMember {

    /** PK */
    private Long id;

    /** 채널 (NOT NULL) */
    private Long channelId;

    /** 참여자 (NOT NULL) */
    private Long accountId;

    /** 초대됨 / 참여 / 나감 / 내보내짐 (NOT NULL) */
    private ChannelMemberStatus status;

    /** 마지막으로 읽은 메시지 ID (안읽은 수 계산, 기기 간 동기화) */
    private Long lastReadMessageId;

    /** 1:1 대화 목록 숨김 시각 (이후 새 메시지가 오면 다시 표시) */
    private LocalDateTime hiddenAt;

    /** 참여 시각 */
    private LocalDateTime joinedAt;

    /** 나간 시각 */
    private LocalDateTime leftAt;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private ChatChannelMember(
            Long id,
            Long channelId,
            Long accountId,
            ChannelMemberStatus status,
            Long lastReadMessageId,
            LocalDateTime hiddenAt,
            LocalDateTime joinedAt,
            LocalDateTime leftAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.channelId = channelId;
        this.accountId = accountId;
        this.status = status;
        this.lastReadMessageId = lastReadMessageId;
        this.hiddenAt = hiddenAt;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static ChatChannelMember reconstitute(
            Long id,
            Long channelId,
            Long accountId,
            ChannelMemberStatus status,
            Long lastReadMessageId,
            LocalDateTime hiddenAt,
            LocalDateTime joinedAt,
            LocalDateTime leftAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new ChatChannelMember(id, channelId, accountId, status, lastReadMessageId, hiddenAt, joinedAt, leftAt, createdAt, updatedAt);
    }
}
