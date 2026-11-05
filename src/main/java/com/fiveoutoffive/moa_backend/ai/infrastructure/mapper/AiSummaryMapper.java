package com.fiveoutoffive.moa_backend.ai.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiSummary;
import com.fiveoutoffive.moa_backend.ai.infrastructure.persistence.AiSummaryJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AiSummaryMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    AiSummaryJpaEntity toJpaEntity(AiSummary aiSummary);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default AiSummary toDomain(AiSummaryJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return AiSummary.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getScopeType(),
                jpaEntity.getContent(),
                jpaEntity.getRequestedByAccountId(),
                jpaEntity.getSavedCardId(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
