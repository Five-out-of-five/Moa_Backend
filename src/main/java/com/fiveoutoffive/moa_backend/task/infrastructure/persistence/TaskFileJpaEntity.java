package com.fiveoutoffive.moa_backend.task.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 업무 첨부 파일 (task_file). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "task_file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskFileJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public TaskFileJpaEntity(
            Long id,
            Long taskId,
            Long fileId,
            Integer sortOrder
    ) {
        this.id = id;
        this.taskId = taskId;
        this.fileId = fileId;
        this.sortOrder = sortOrder;
    }
}
