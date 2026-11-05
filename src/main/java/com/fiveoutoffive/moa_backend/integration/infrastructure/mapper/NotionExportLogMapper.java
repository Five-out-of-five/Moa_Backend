package com.fiveoutoffive.moa_backend.integration.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.integration.domain.model.NotionExportLog;
import com.fiveoutoffive.moa_backend.integration.infrastructure.persistence.NotionExportLogJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotionExportLogMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    NotionExportLogJpaEntity toJpaEntity(NotionExportLog notionExportLog);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default NotionExportLog toDomain(NotionExportLogJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return NotionExportLog.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getIntegrationId(),
                jpaEntity.getSessionId(),
                jpaEntity.getNotionPageId(),
                jpaEntity.getStatus(),
                jpaEntity.getErrorMessage(),
                jpaEntity.getExportedAt()
        );
    }
}
