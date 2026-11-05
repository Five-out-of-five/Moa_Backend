package com.fiveoutoffive.moa_backend.ideacard.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardFile;
import com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence.CardFileJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardFileMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    CardFileJpaEntity toJpaEntity(CardFile cardFile);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default CardFile toDomain(CardFileJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return CardFile.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getCardId(),
                jpaEntity.getFileId(),
                jpaEntity.getSortOrder()
        );
    }
}
