package com.fiveoutoffive.moa_backend.task.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.task.domain.model.Task;
import com.fiveoutoffive.moa_backend.task.infrastructure.persistence.TaskJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    TaskJpaEntity toJpaEntity(Task task);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default Task toDomain(TaskJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return Task.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getSprintId(),
                jpaEntity.getTitle(),
                jpaEntity.getDescription(),
                jpaEntity.getStatus(),
                jpaEntity.getPriority(),
                jpaEntity.getRegistrantAccountId(),
                jpaEntity.getAssigneeAccountId(),
                jpaEntity.getDueDate(),
                jpaEntity.getSortOrder(),
                jpaEntity.getSourceType(),
                jpaEntity.getSourceCardId(),
                jpaEntity.getSourceVoteId(),
                jpaEntity.getCompletedAt(),
                jpaEntity.getDueAlertSentAt(),
                jpaEntity.getVersion(),
                jpaEntity.getDeletedAt(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
