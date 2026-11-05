package com.fiveoutoffive.moa_backend.ideacard.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.IdeaCard;
import com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence.IdeaCardJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IdeaCardMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    IdeaCardJpaEntity toJpaEntity(IdeaCard ideaCard);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default IdeaCard toDomain(IdeaCardJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return IdeaCard.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getSessionId(),
                jpaEntity.getCardType(),
                jpaEntity.getTitle(),
                jpaEntity.getContent(),
                jpaEntity.getWriterAccountId(),
                jpaEntity.getSortOrder(),
                jpaEntity.getIsAiGenerated(),
                jpaEntity.getVersion(),
                jpaEntity.getEditedAt(),
                jpaEntity.getDeletedAt(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
