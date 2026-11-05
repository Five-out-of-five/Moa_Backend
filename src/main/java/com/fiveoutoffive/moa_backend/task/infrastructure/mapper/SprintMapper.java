package com.fiveoutoffive.moa_backend.task.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.task.domain.model.Sprint;
import com.fiveoutoffive.moa_backend.task.infrastructure.persistence.SprintJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SprintMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    SprintJpaEntity toJpaEntity(Sprint sprint);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default Sprint toDomain(SprintJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return Sprint.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getSprintNo(),
                jpaEntity.getName(),
                jpaEntity.getStartDate(),
                jpaEntity.getEndDate(),
                jpaEntity.getStatus(),
                jpaEntity.getStartedByAccountId(),
                jpaEntity.getClosedByAccountId(),
                jpaEntity.getClosedAt(),
                jpaEntity.getTotalTaskCount(),
                jpaEntity.getDoneTaskCount(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
