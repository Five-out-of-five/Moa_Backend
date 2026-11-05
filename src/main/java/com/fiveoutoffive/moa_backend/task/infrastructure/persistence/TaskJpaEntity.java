package com.fiveoutoffive.moa_backend.task.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.task.domain.model.TaskPriority;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskSourceType;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 업무(Task) (task). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "task")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "sprint_id")
    private Long sprintId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private TaskPriority priority;

    @Column(name = "registrant_account_id", nullable = false)
    private Long registrantAccountId;

    @Column(name = "assignee_account_id")
    private Long assigneeAccountId;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 10)
    private TaskSourceType sourceType;

    @Column(name = "source_card_id")
    private Long sourceCardId;

    @Column(name = "source_vote_id")
    private Long sourceVoteId;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "due_alert_sent_at")
    private LocalDateTime dueAlertSentAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public TaskJpaEntity(
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

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.status == null) {
            this.status = TaskStatus.TODO;
        }
        if (this.priority == null) {
            this.priority = TaskPriority.NORMAL;
        }
        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
        if (this.sourceType == null) {
            this.sourceType = TaskSourceType.NONE;
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
