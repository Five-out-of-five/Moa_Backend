package com.fiveoutoffive.moa_backend.integration.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.integration.domain.model.SlackDeliveryLog;
import com.fiveoutoffive.moa_backend.integration.infrastructure.persistence.SlackDeliveryLogJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SlackDeliveryLogMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    SlackDeliveryLogJpaEntity toJpaEntity(SlackDeliveryLog slackDeliveryLog);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default SlackDeliveryLog toDomain(SlackDeliveryLogJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return SlackDeliveryLog.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getIntegrationId(),
                jpaEntity.getEventType(),
                jpaEntity.getStatus(),
                jpaEntity.getRetryCount(),
                jpaEntity.getErrorMessage(),
                jpaEntity.getCreatedAt()
        );
    }
}
