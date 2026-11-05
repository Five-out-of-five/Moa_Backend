package com.fiveoutoffive.moa_backend.ai.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiCallLog;
import com.fiveoutoffive.moa_backend.ai.infrastructure.persistence.AiCallLogJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AiCallLogMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    AiCallLogJpaEntity toJpaEntity(AiCallLog aiCallLog);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default AiCallLog toDomain(AiCallLogJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return AiCallLog.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getFeature(),
                jpaEntity.getRoomId(),
                jpaEntity.getAccountId(),
                jpaEntity.getRefType(),
                jpaEntity.getRefId(),
                jpaEntity.getModel(),
                jpaEntity.getPromptTokens(),
                jpaEntity.getOutputTokens(),
                jpaEntity.getCostAmount(),
                jpaEntity.getLatencyMs(),
                jpaEntity.getStatus(),
                jpaEntity.getErrorMessage(),
                jpaEntity.getCreatedAt()
        );
    }
}
