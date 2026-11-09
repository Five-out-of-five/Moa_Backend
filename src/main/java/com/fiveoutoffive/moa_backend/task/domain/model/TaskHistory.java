package com.fiveoutoffive.moa_backend.task.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 업무 상태·담당자 변경 이력 (task_history)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskHistory {

    /** PK */
    private Long id;

    /** 업무 (NOT NULL) */
    private Long taskId;

    /** 이력 종류 (UNASSIGNED = 담당자가 방을 나가 미담당 전환) (NOT NULL) */
    private TaskHistoryType historyType;

    /** 변경 전 상태 */
    private String fromStatus;

    /** 변경 후 상태 */
    private String toStatus;

    /** 변경 전 담당자 */
    private Long fromAssigneeId;

    /** 변경 후 담당자 */
    private Long toAssigneeId;

    /** 변경한 멤버 (NOT NULL) */
    private Long actorAccountId;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    private TaskHistory(
            Long id,
            Long taskId,
            TaskHistoryType historyType,
            String fromStatus,
            String toStatus,
            Long fromAssigneeId,
            Long toAssigneeId,
            Long actorAccountId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.taskId = taskId;
        this.historyType = historyType;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.fromAssigneeId = fromAssigneeId;
        this.toAssigneeId = toAssigneeId;
        this.actorAccountId = actorAccountId;
        this.createdAt = createdAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static TaskHistory reconstitute(
            Long id,
            Long taskId,
            TaskHistoryType historyType,
            String fromStatus,
            String toStatus,
            Long fromAssigneeId,
            Long toAssigneeId,
            Long actorAccountId,
            LocalDateTime createdAt
    ) {
        return new TaskHistory(id, taskId, historyType, fromStatus, toStatus, fromAssigneeId, toAssigneeId, actorAccountId, createdAt);
    }
}
