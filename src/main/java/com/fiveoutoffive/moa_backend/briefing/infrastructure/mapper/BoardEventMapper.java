package com.fiveoutoffive.moa_backend.briefing.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEvent;
import com.fiveoutoffive.moa_backend.briefing.infrastructure.persistence.BoardEventJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BoardEventMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    BoardEventJpaEntity toJpaEntity(BoardEvent boardEvent);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default BoardEvent toDomain(BoardEventJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return BoardEvent.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getEventType(),
                jpaEntity.getActorAccountId(),
                jpaEntity.getRecipientAccountId(),
                jpaEntity.getTargetType(),
                jpaEntity.getTargetId(),
                jpaEntity.getPayload(),
                jpaEntity.getCreatedAt()
        );
    }
}
