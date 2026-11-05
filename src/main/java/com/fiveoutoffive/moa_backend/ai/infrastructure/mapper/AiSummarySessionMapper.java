package com.fiveoutoffive.moa_backend.ai.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiSummarySession;
import com.fiveoutoffive.moa_backend.ai.infrastructure.persistence.AiSummarySessionJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AiSummarySessionMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    AiSummarySessionJpaEntity toJpaEntity(AiSummarySession aiSummarySession);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default AiSummarySession toDomain(AiSummarySessionJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return AiSummarySession.reconstitute(
                jpaEntity.getSummaryId(),
                jpaEntity.getSessionId()
        );
    }
}
