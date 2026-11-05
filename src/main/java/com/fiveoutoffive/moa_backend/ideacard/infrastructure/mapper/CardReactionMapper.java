package com.fiveoutoffive.moa_backend.ideacard.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardReaction;
import com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence.CardReactionJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardReactionMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    CardReactionJpaEntity toJpaEntity(CardReaction cardReaction);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default CardReaction toDomain(CardReactionJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return CardReaction.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getCardId(),
                jpaEntity.getAccountId(),
                jpaEntity.getEmoji(),
                jpaEntity.getCreatedAt()
        );
    }
}
