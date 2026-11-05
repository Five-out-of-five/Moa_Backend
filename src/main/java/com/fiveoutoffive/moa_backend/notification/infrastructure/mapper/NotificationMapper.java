package com.fiveoutoffive.moa_backend.notification.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.notification.domain.model.Notification;
import com.fiveoutoffive.moa_backend.notification.infrastructure.persistence.NotificationJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    NotificationJpaEntity toJpaEntity(Notification notification);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default Notification toDomain(NotificationJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return Notification.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getOwnerAccountId(),
                jpaEntity.getRoomId(),
                jpaEntity.getNotificationGroup(),
                jpaEntity.getNotificationType(),
                jpaEntity.getTitle(),
                jpaEntity.getContent(),
                jpaEntity.getTargetType(),
                jpaEntity.getTargetId(),
                jpaEntity.getRead(),
                jpaEntity.getReadAt(),
                jpaEntity.getCreatedAt()
        );
    }
}
