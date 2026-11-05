package com.fiveoutoffive.moa_backend.room.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.room.domain.model.RoomJoinRequest;
import com.fiveoutoffive.moa_backend.room.infrastructure.persistence.RoomJoinRequestJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoomJoinRequestMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    RoomJoinRequestJpaEntity toJpaEntity(RoomJoinRequest roomJoinRequest);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default RoomJoinRequest toDomain(RoomJoinRequestJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return RoomJoinRequest.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getAccountId(),
                jpaEntity.getStatus(),
                jpaEntity.getRequestedAt(),
                jpaEntity.getExpiresAt(),
                jpaEntity.getProcessedAt(),
                jpaEntity.getProcessedByAccountId()
        );
    }
}
