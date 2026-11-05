package com.fiveoutoffive.moa_backend.mediation.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.mediation.domain.model.CardConflict;
import com.fiveoutoffive.moa_backend.mediation.infrastructure.persistence.CardConflictJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardConflictMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    CardConflictJpaEntity toJpaEntity(CardConflict cardConflict);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default CardConflict toDomain(CardConflictJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return CardConflict.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getCardAId(),
                jpaEntity.getCardBId(),
                jpaEntity.getStatus(),
                jpaEntity.getReason(),
                jpaEntity.getDismissedByAccountId(),
                jpaEntity.getDismissedAt(),
                jpaEntity.getAdoptedCardId(),
                jpaEntity.getDetectedAt(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
