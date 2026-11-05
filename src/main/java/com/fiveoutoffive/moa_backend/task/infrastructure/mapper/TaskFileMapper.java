package com.fiveoutoffive.moa_backend.task.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.task.domain.model.TaskFile;
import com.fiveoutoffive.moa_backend.task.infrastructure.persistence.TaskFileJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskFileMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    TaskFileJpaEntity toJpaEntity(TaskFile taskFile);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default TaskFile toDomain(TaskFileJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return TaskFile.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getTaskId(),
                jpaEntity.getFileId(),
                jpaEntity.getSortOrder()
        );
    }
}
