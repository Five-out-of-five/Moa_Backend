package com.fiveoutoffive.moa_backend.session.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.session.domain.model.BoardSession;
import com.fiveoutoffive.moa_backend.session.infrastructure.persistence.BoardSessionJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BoardSessionMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    BoardSessionJpaEntity toJpaEntity(BoardSession boardSession);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default BoardSession toDomain(BoardSessionJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return BoardSession.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getSessionNo(),
                jpaEntity.getTitle(),
                jpaEntity.getStatus(),
                jpaEntity.getDeadlineAt(),
                jpaEntity.getStartedByAccountId(),
                jpaEntity.getClosedByAccountId(),
                jpaEntity.getStartedAt(),
                jpaEntity.getClosedAt(),
                jpaEntity.getCreatedAt()
        );
    }
}
