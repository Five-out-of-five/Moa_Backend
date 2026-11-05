package com.fiveoutoffive.moa_backend.ai.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiAssistantMessage;
import com.fiveoutoffive.moa_backend.ai.infrastructure.persistence.AiAssistantMessageJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AiAssistantMessageMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    AiAssistantMessageJpaEntity toJpaEntity(AiAssistantMessage aiAssistantMessage);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default AiAssistantMessage toDomain(AiAssistantMessageJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return AiAssistantMessage.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getAccountId(),
                jpaEntity.getQuestion(),
                jpaEntity.getAnswer(),
                jpaEntity.getStatus(),
                jpaEntity.getReferenceJson(),
                jpaEntity.getCreatedAt()
        );
    }
}
