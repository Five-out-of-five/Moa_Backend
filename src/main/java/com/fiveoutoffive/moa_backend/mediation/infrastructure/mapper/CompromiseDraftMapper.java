package com.fiveoutoffive.moa_backend.mediation.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.mediation.domain.model.CompromiseDraft;
import com.fiveoutoffive.moa_backend.mediation.infrastructure.persistence.CompromiseDraftJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompromiseDraftMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    CompromiseDraftJpaEntity toJpaEntity(CompromiseDraft compromiseDraft);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default CompromiseDraft toDomain(CompromiseDraftJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return CompromiseDraft.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getConflictId(),
                jpaEntity.getRequestedByAccountId(),
                jpaEntity.getCardType(),
                jpaEntity.getTitle(),
                jpaEntity.getContent(),
                jpaEntity.getStatus(),
                jpaEntity.getAdoptedCardId(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
