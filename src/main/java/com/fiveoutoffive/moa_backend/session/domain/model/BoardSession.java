package com.fiveoutoffive.moa_backend.session.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 아이디어 보드 회차 (멤버가 직접 시작, 진행 중 회차는 방당 0~1개) (board_session)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardSession {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 회차 번호 (방 안에서 1부터 증가, 생성 시 최대값 + 1) (NOT NULL) */
    private Integer sessionNo;

    /** 회차명 (기본값 예: '1차') (NOT NULL) */
    private String title;

    /** 진행중 / 마감 (NOT NULL) */
    private SessionStatus status;

    /** 마감 시각 (NULL = 무기한, 수동 마감) */
    private LocalDateTime deadlineAt;

    /** 회차를 시작한 멤버 (NOT NULL) */
    private Long startedByAccountId;

    /** 수동 마감한 멤버 (자동 마감이면 NULL) */
    private Long closedByAccountId;

    /** 시작 시각 (NOT NULL) */
    private LocalDateTime startedAt;

    /** 마감 시각 */
    private LocalDateTime closedAt;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    private BoardSession(
            Long id,
            Long roomId,
            Integer sessionNo,
            String title,
            SessionStatus status,
            LocalDateTime deadlineAt,
            Long startedByAccountId,
            Long closedByAccountId,
            LocalDateTime startedAt,
            LocalDateTime closedAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.sessionNo = sessionNo;
        this.title = title;
        this.status = status;
        this.deadlineAt = deadlineAt;
        this.startedByAccountId = startedByAccountId;
        this.closedByAccountId = closedByAccountId;
        this.startedAt = startedAt;
        this.closedAt = closedAt;
        this.createdAt = createdAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static BoardSession reconstitute(
            Long id,
            Long roomId,
            Integer sessionNo,
            String title,
            SessionStatus status,
            LocalDateTime deadlineAt,
            Long startedByAccountId,
            Long closedByAccountId,
            LocalDateTime startedAt,
            LocalDateTime closedAt,
            LocalDateTime createdAt
    ) {
        return new BoardSession(id, roomId, sessionNo, title, status, deadlineAt, startedByAccountId, closedByAccountId, startedAt, closedAt, createdAt);
    }
}
