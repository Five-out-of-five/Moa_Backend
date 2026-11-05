package com.fiveoutoffive.moa_backend.dashboard.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.dashboard.domain.model.RoomWidget;
import com.fiveoutoffive.moa_backend.dashboard.infrastructure.persistence.RoomWidgetJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoomWidgetMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    RoomWidgetJpaEntity toJpaEntity(RoomWidget roomWidget);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default RoomWidget toDomain(RoomWidgetJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return RoomWidget.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getWidgetType(),
                jpaEntity.getPosX(),
                jpaEntity.getPosY(),
                jpaEntity.getWidth(),
                jpaEntity.getHeight(),
                jpaEntity.getUpdatedByAccountId(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
