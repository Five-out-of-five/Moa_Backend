package com.fiveoutoffive.moa_backend.dashboard.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 방 대시보드 위젯 배치 (방당 1개 팀 공용, 개인별 배치 없음) (room_widget)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomWidget {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 위젯 종류: 상태값/진척도/아이디어보드/업무보드/투표/캐치업브리핑/중재 (NOT NULL) */
    private WidgetType widgetType;

    /** 그리드 x (0~11) (NOT NULL) */
    private Integer posX;

    /** 그리드 y (세로 제한 없음) (NOT NULL) */
    private Integer posY;

    /** 가로 칸 수 (1~12) (NOT NULL) */
    private Integer width;

    /** 세로 칸 수 (1~8) (NOT NULL) */
    private Integer height;

    /** 마지막으로 변경한 멤버 */
    private Long updatedByAccountId;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private RoomWidget(
            Long id,
            Long roomId,
            WidgetType widgetType,
            Integer posX,
            Integer posY,
            Integer width,
            Integer height,
            Long updatedByAccountId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.widgetType = widgetType;
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.updatedByAccountId = updatedByAccountId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static RoomWidget reconstitute(
            Long id,
            Long roomId,
            WidgetType widgetType,
            Integer posX,
            Integer posY,
            Integer width,
            Integer height,
            Long updatedByAccountId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new RoomWidget(id, roomId, widgetType, posX, posY, width, height, updatedByAccountId, createdAt, updatedAt);
    }
}
