package com.fiveoutoffive.moa_backend.vote.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.vote.domain.model.VoteBallot;
import com.fiveoutoffive.moa_backend.vote.infrastructure.persistence.VoteBallotJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VoteBallotMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    VoteBallotJpaEntity toJpaEntity(VoteBallot voteBallot);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default VoteBallot toDomain(VoteBallotJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return VoteBallot.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getVoteId(),
                jpaEntity.getOptionId(),
                jpaEntity.getAccountId(),
                jpaEntity.getVotedAt()
        );
    }
}
