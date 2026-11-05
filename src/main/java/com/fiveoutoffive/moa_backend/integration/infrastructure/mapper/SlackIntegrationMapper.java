package com.fiveoutoffive.moa_backend.integration.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.integration.domain.model.SlackIntegration;
import com.fiveoutoffive.moa_backend.integration.infrastructure.persistence.SlackIntegrationJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SlackIntegrationMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    SlackIntegrationJpaEntity toJpaEntity(SlackIntegration slackIntegration);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default SlackIntegration toDomain(SlackIntegrationJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return SlackIntegration.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getWebhookUrlEnc(),
                jpaEntity.getEventTypes(),
                jpaEntity.getEnabled(),
                jpaEntity.getConsecutiveFailCount(),
                jpaEntity.getDisabledAt(),
                jpaEntity.getCreatedByAccountId(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
