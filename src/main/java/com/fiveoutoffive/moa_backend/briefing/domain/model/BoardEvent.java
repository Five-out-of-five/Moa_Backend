package com.fiveoutoffive.moa_backend.briefing.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 방 변경 이벤트 로그 (캐치업 브리핑 원천, 30일 보관) (board_event)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardEvent {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 이벤트 종류 (NOT NULL) */
    private BoardEventType eventType;

    /** 이벤트를 일으킨 멤버 (본인 이벤트는 집계 제외) */
    private Long actorAccountId;

    /** '내가 할 일' 대상 (배정받은 담당자 등) */
    private Long recipientAccountId;

    /** 대상 종류 (NOT NULL) */
    private BoardEventTargetType targetType;

    /** 대상 ID (항목 클릭 시 이동·강조) (NOT NULL) */
    private Long targetId;

    /** 표시용 부가 정보 */
    private String payload;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    private BoardEvent(
            Long id,
            Long roomId,
            BoardEventType eventType,
            Long actorAccountId,
            Long recipientAccountId,
            BoardEventTargetType targetType,
            Long targetId,
            String payload,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.eventType = eventType;
        this.actorAccountId = actorAccountId;
        this.recipientAccountId = recipientAccountId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static BoardEvent reconstitute(
            Long id,
            Long roomId,
            BoardEventType eventType,
            Long actorAccountId,
            Long recipientAccountId,
            BoardEventTargetType targetType,
            Long targetId,
            String payload,
            LocalDateTime createdAt
    ) {
        return new BoardEvent(id, roomId, eventType, actorAccountId, recipientAccountId, targetType, targetId, payload, createdAt);
    }
}
