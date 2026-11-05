package com.fiveoutoffive.moa_backend.mood.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.mood.domain.model.MoodStatus;
import com.fiveoutoffive.moa_backend.mood.infrastructure.persistence.MoodStatusJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MoodStatusMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    MoodStatusJpaEntity toJpaEntity(MoodStatus moodStatus);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default MoodStatus toDomain(MoodStatusJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return MoodStatus.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getAccountId(),
                jpaEntity.getMood(),
                jpaEntity.getComment(),
                jpaEntity.getRecordedDate(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
