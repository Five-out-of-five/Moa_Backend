package com.fiveoutoffive.moa_backend.room.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.room.domain.model.Room;
import com.fiveoutoffive.moa_backend.room.infrastructure.persistence.RoomJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoomMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    RoomJpaEntity toJpaEntity(Room room);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default Room toDomain(RoomJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return Room.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getName(),
                jpaEntity.getDescription(),
                jpaEntity.getImageFileId(),
                jpaEntity.getInviteCode(),
                jpaEntity.getStatus(),
                jpaEntity.getInactiveReason(),
                jpaEntity.getInactivatedAt(),
                jpaEntity.getInactivatedByAccountId(),
                jpaEntity.getCreatedByAccountId(),
                jpaEntity.getLayoutVersion(),
                jpaEntity.getLastActivityAt(),
                jpaEntity.getDeletedAt(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
