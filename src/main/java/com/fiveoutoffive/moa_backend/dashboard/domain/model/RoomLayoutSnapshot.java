package com.fiveoutoffive.moa_backend.dashboard.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 구성 적용 직전 배치 (되돌리기 1회용) (room_layout_snapshot)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomLayoutSnapshot {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 적용 직전 위젯 배치 전체 (NOT NULL) */
    private String layoutJson;

    /** AI 구성을 적용한 멤버 */
    private Long createdByAccountId;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    private RoomLayoutSnapshot(
            Long id,
            Long roomId,
            String layoutJson,
            Long createdByAccountId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.layoutJson = layoutJson;
        this.createdByAccountId = createdByAccountId;
        this.createdAt = createdAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static RoomLayoutSnapshot reconstitute(
            Long id,
            Long roomId,
            String layoutJson,
            Long createdByAccountId,
            LocalDateTime createdAt
    ) {
        return new RoomLayoutSnapshot(id, roomId, layoutJson, createdByAccountId, createdAt);
    }
}
