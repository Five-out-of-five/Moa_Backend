package com.fiveoutoffive.moa_backend.vote.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.vote.domain.model.Vote;
import com.fiveoutoffive.moa_backend.vote.infrastructure.persistence.VoteJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VoteMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    VoteJpaEntity toJpaEntity(Vote vote);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default Vote toDomain(VoteJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return Vote.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getSourceCardId(),
                jpaEntity.getParentVoteId(),
                jpaEntity.getTitle(),
                jpaEntity.getVoteType(),
                jpaEntity.getMaxSelectCount(),
                jpaEntity.getIsAnonymous(),
                jpaEntity.getStatus(),
                jpaEntity.getDeadlineAt(),
                jpaEntity.getCloseReason(),
                jpaEntity.getClosedAt(),
                jpaEntity.getClosedByAccountId(),
                jpaEntity.getCreatorAccountId(),
                jpaEntity.getReminderSentAt(),
                jpaEntity.getVersion(),
                jpaEntity.getDeletedAt(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
