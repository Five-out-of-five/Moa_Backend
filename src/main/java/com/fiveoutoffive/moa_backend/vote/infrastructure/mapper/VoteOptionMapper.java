package com.fiveoutoffive.moa_backend.vote.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.vote.domain.model.VoteOption;
import com.fiveoutoffive.moa_backend.vote.infrastructure.persistence.VoteOptionJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VoteOptionMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    VoteOptionJpaEntity toJpaEntity(VoteOption voteOption);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default VoteOption toDomain(VoteOptionJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return VoteOption.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getVoteId(),
                jpaEntity.getContent(),
                jpaEntity.getSortOrder()
        );
    }
}
