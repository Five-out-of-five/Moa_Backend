package com.fiveoutoffive.moa_backend.integration.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.integration.domain.model.NotionIntegration;
import com.fiveoutoffive.moa_backend.integration.infrastructure.persistence.NotionIntegrationJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotionIntegrationMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    NotionIntegrationJpaEntity toJpaEntity(NotionIntegration notionIntegration);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default NotionIntegration toDomain(NotionIntegrationJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return NotionIntegration.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getAccessTokenEnc(),
                jpaEntity.getNotionWorkspaceId(),
                jpaEntity.getNotionWorkspaceName(),
                jpaEntity.getTargetDatabaseId(),
                jpaEntity.getExportMode(),
                jpaEntity.getEnabled(),
                jpaEntity.getTokenExpiredAt(),
                jpaEntity.getCreatedByAccountId(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
