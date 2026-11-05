package com.fiveoutoffive.moa_backend.task.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.task.domain.model.TaskHistory;
import com.fiveoutoffive.moa_backend.task.infrastructure.persistence.TaskHistoryJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskHistoryMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    TaskHistoryJpaEntity toJpaEntity(TaskHistory taskHistory);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default TaskHistory toDomain(TaskHistoryJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return TaskHistory.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getTaskId(),
                jpaEntity.getHistoryType(),
                jpaEntity.getFromStatus(),
                jpaEntity.getToStatus(),
                jpaEntity.getFromAssigneeId(),
                jpaEntity.getToAssigneeId(),
                jpaEntity.getActorAccountId(),
                jpaEntity.getCreatedAt()
        );
    }
}
