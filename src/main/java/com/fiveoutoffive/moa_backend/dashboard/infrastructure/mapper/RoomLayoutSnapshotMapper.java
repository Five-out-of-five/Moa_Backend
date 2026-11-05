package com.fiveoutoffive.moa_backend.dashboard.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.dashboard.domain.model.RoomLayoutSnapshot;
import com.fiveoutoffive.moa_backend.dashboard.infrastructure.persistence.RoomLayoutSnapshotJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoomLayoutSnapshotMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    RoomLayoutSnapshotJpaEntity toJpaEntity(RoomLayoutSnapshot roomLayoutSnapshot);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default RoomLayoutSnapshot toDomain(RoomLayoutSnapshotJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return RoomLayoutSnapshot.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getLayoutJson(),
                jpaEntity.getCreatedByAccountId(),
                jpaEntity.getCreatedAt()
        );
    }
}
