package com.fiveoutoffive.moa_backend.room.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.room.domain.model.RoomMember;
import com.fiveoutoffive.moa_backend.room.infrastructure.persistence.RoomMemberJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoomMemberMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    RoomMemberJpaEntity toJpaEntity(RoomMember roomMember);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default RoomMember toDomain(RoomMemberJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return RoomMember.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getAccountId(),
                jpaEntity.getRole(),
                jpaEntity.getStatus(),
                jpaEntity.getNickname(),
                jpaEntity.getProfileFileId(),
                jpaEntity.getPositionLabel(),
                jpaEntity.getNotificationMuted(),
                jpaEntity.getLastBriefingCheckedAt(),
                jpaEntity.getProfileSetupAt(),
                jpaEntity.getJoinedAt(),
                jpaEntity.getLeftAt(),
                jpaEntity.getKickedAt(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
