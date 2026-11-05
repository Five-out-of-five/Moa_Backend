package com.fiveoutoffive.moa_backend.task.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 스프린트 (진척도 기준 기간, 진행 중 스프린트는 방당 0~1개) (sprint)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sprint {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 스프린트 번호 (방 안에서 1부터 증가, 생성 시 최대값 + 1) (NOT NULL) */
    private Integer sprintNo;

    /** 스프린트 이름 (NOT NULL) */
    private String name;

    /** 시작일 (한국 날짜) (NOT NULL) */
    private LocalDate startDate;

    /** 종료일 (한국 날짜). 이 날이 지나면 스케줄러가 자동 마감 (NOT NULL) */
    private LocalDate endDate;

    /** 진행중 / 마감 (NOT NULL) */
    private SprintStatus status;

    /** 스프린트를 시작한 멤버 (NOT NULL) */
    private Long startedByAccountId;

    /** 수동 마감한 멤버 (자동 마감이면 NULL) */
    private Long closedByAccountId;

    /** 마감 시각 */
    private LocalDateTime closedAt;

    /** 마감 시점 업무 수 (마감 후 진척 기록용) */
    private Integer totalTaskCount;

    /** 마감 시점 완료 업무 수 */
    private Integer doneTaskCount;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private Sprint(
            Long id,
            Long roomId,
            Integer sprintNo,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            SprintStatus status,
            Long startedByAccountId,
            Long closedByAccountId,
            LocalDateTime closedAt,
            Integer totalTaskCount,
            Integer doneTaskCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.sprintNo = sprintNo;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.startedByAccountId = startedByAccountId;
        this.closedByAccountId = closedByAccountId;
        this.closedAt = closedAt;
        this.totalTaskCount = totalTaskCount;
        this.doneTaskCount = doneTaskCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static Sprint reconstitute(
            Long id,
            Long roomId,
            Integer sprintNo,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            SprintStatus status,
            Long startedByAccountId,
            Long closedByAccountId,
            LocalDateTime closedAt,
            Integer totalTaskCount,
            Integer doneTaskCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new Sprint(id, roomId, sprintNo, name, startDate, endDate, status, startedByAccountId, closedByAccountId, closedAt, totalTaskCount, doneTaskCount, createdAt, updatedAt);
    }
}
