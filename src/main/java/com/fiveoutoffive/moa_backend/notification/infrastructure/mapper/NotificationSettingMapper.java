package com.fiveoutoffive.moa_backend.notification.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationSetting;
import com.fiveoutoffive.moa_backend.notification.infrastructure.persistence.NotificationSettingJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationSettingMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    NotificationSettingJpaEntity toJpaEntity(NotificationSetting notificationSetting);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default NotificationSetting toDomain(NotificationSettingJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return NotificationSetting.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getAccountId(),
                jpaEntity.getRoomId(),
                jpaEntity.getNotificationGroup(),
                jpaEntity.getEnabled(),
                jpaEntity.getUpdatedAt()
        );
    }
}
