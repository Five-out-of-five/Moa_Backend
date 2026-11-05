package com.fiveoutoffive.moa_backend.task.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.task.domain.model.SprintStatus;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 스프린트 (진척도 기준 기간, 진행 중 스프린트는 방당 0~1개) (sprint). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "sprint")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SprintJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "sprint_no", nullable = false)
    private Integer sprintNo;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SprintStatus status;

    @Column(name = "started_by_account_id", nullable = false)
    private Long startedByAccountId;

    @Column(name = "closed_by_account_id")
    private Long closedByAccountId;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "total_task_count")
    private Integer totalTaskCount;

    @Column(name = "done_task_count")
    private Integer doneTaskCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public SprintJpaEntity(
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

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.status == null) {
            this.status = SprintStatus.IN_PROGRESS;
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
