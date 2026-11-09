package com.fiveoutoffive.moa_backend.task.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 업무(Task) (task)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 소속 스프린트 (NULL = 백로그). 등록 시 진행 중 스프린트에 자동 소속 */
    private Long sprintId;

    /** 업무명 (NOT NULL) */
    private String title;

    /** 설명 */
    private String description;

    /** 대기 / 진행 / 검토 / 완료 (반려는 상태가 아닌 담당자 반환 동작) (NOT NULL) */
    private TaskStatus status;

    /** 중요도 (미선택 시 보통) (NOT NULL) */
    private TaskPriority priority;

    /** 등록자 (NOT NULL) */
    private Long registrantAccountId;

    /** 담당자 (1업무 1담당자). 등록 시 필수. 반려 시 등록자로 변경. 담당자가 방을 나가거나 탈퇴하면 NULL(미담당) → 등록자가 다시 배정 */
    private Long assigneeAccountId;

    /** 마감일 (NULL 허용) */
    private LocalDate dueDate;

    /** 같은 상태(칸반 칸) 안에서의 표시 순서 (NOT NULL) */
    private Integer sortOrder;

    /** 출처 종류 (NOT NULL) */
    private TaskSourceType sourceType;

    /** 출처 카드 */
    private Long sourceCardId;

    /** 출처 투표 결과 */
    private Long sourceVoteId;

    /** 완료 시각 (되돌리면 초기화) */
    private LocalDateTime completedAt;

    /** 마감 임박 알림 발송 시각 (1회만, 마감일이 바뀌면 초기화) */
    private LocalDateTime dueAlertSentAt;

    /** 낙관적 락 (동시에 같은 업무를 이동하면 나중 요청 거절) (NOT NULL) */
    private Long version;

    /** 소프트 딜리트 시각 (NULL = 정상) */
    private LocalDateTime deletedAt;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private Task(
            Long id,
            Long roomId,
            Long sprintId,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            Long registrantAccountId,
            Long assigneeAccountId,
            LocalDate dueDate,
            Integer sortOrder,
            TaskSourceType sourceType,
            Long sourceCardId,
            Long sourceVoteId,
            LocalDateTime completedAt,
            LocalDateTime dueAlertSentAt,
            Long version,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.sprintId = sprintId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.registrantAccountId = registrantAccountId;
        this.assigneeAccountId = assigneeAccountId;
        this.dueDate = dueDate;
        this.sortOrder = sortOrder;
        this.sourceType = sourceType;
        this.sourceCardId = sourceCardId;
        this.sourceVoteId = sourceVoteId;
        this.completedAt = completedAt;
        this.dueAlertSentAt = dueAlertSentAt;
        this.version = version;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static Task reconstitute(
            Long id,
            Long roomId,
            Long sprintId,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            Long registrantAccountId,
            Long assigneeAccountId,
            LocalDate dueDate,
            Integer sortOrder,
            TaskSourceType sourceType,
            Long sourceCardId,
            Long sourceVoteId,
            LocalDateTime completedAt,
            LocalDateTime dueAlertSentAt,
            Long version,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new Task(id, roomId, sprintId, title, description, status, priority, registrantAccountId, assigneeAccountId, dueDate, sortOrder, sourceType, sourceCardId, sourceVoteId, completedAt, dueAlertSentAt, version, deletedAt, createdAt, updatedAt);
    }
}
