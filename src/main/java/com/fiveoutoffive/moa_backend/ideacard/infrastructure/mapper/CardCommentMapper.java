package com.fiveoutoffive.moa_backend.ideacard.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardComment;
import com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence.CardCommentJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardCommentMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    CardCommentJpaEntity toJpaEntity(CardComment cardComment);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default CardComment toDomain(CardCommentJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return CardComment.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getCardId(),
                jpaEntity.getWriterAccountId(),
                jpaEntity.getContent(),
                jpaEntity.getEditedAt(),
                jpaEntity.getDeletedAt(),
                jpaEntity.getCreatedAt()
        );
    }
}
